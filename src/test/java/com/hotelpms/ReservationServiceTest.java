package com.hotelpms;

import com.hotelpms.api.dto.ReservationResponse;
import com.hotelpms.api.dto.ReserveRequest;
import com.hotelpms.domain.ReservationStatus;
import com.hotelpms.domain.entity.Reservation;
import com.hotelpms.domain.entity.RoomInventory;
import com.hotelpms.domain.entity.RoomType;
import com.hotelpms.repository.ReservationRepository;
import com.hotelpms.repository.RoomInventoryRepository;
import com.hotelpms.repository.RoomTypeRepository;
import com.hotelpms.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private RoomTypeRepository rtRepo;
    private RoomInventoryRepository invRepo;
    private ReservationRepository resRepo;
    private ReservationService service;

    private RoomType rt;

    @BeforeEach
    void setUp() {
        rtRepo = mock(RoomTypeRepository.class);
        invRepo = mock(RoomInventoryRepository.class);
        resRepo = mock(ReservationRepository.class);
        service = new ReservationService(rtRepo, invRepo, resRepo);

        rt = new RoomType();
        rt.setId(1L);
        rt.setHotelId(1L);
        rt.setName("Deluxe");
        rt.setTotalRooms(10);
        rt.setBasePrice(new BigDecimal("1000000"));
        rt.setCurrency("VND");
    }

    /** PMS-02: reserve phai dung finder co PESSIMISTIC_WRITE lock (findByIdForUpdate), khong dung findById. */
    @Test
    void reserveUsesLockingFinder() {
        when(rtRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(rt));
        when(invRepo.findByRoomTypeIdAndDate(eq(1L), any())).thenReturn(Optional.empty());
        when(resRepo.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReserveRequest req = new ReserveRequest(1L, "Alice",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3), 1);

        ReservationResponse resp = service.reserve(1L, req);

        verify(rtRepo).findByIdForUpdate(1L);
        verify(rtRepo, never()).findById(any());
        assertEquals(ReservationStatus.CONFIRMED_VALUE, resp.status());
        // 2 dem, default price = basePrice 1.000.000 -> total 2.000.000
        assertEquals(0, new BigDecimal("2000000").compareTo(resp.totalPrice()));
    }

    /**
     * PMS-04: override row co price = null khong gay NPE; nightly fallback ve basePrice.
     * PMS-03: row override moi materialize chi set availableRooms, khong snapshot price.
     */
    @Test
    void reserveWithNullPriceOverrideFallsBackToBasePrice() {
        RoomInventory ovNullPrice = new RoomInventory();
        ovNullPrice.setRoomTypeId(1L);
        ovNullPrice.setAvailableRooms(5);
        ovNullPrice.setPrice(null); // override ton tai nhung price NULL

        LocalDate night = LocalDate.of(2026, 7, 1);
        ovNullPrice.setDate(night);

        when(rtRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(rt));
        when(invRepo.findByRoomTypeIdAndDate(1L, night)).thenReturn(Optional.of(ovNullPrice));
        when(resRepo.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReserveRequest req = new ReserveRequest(1L, "Bob",
                night, night.plusDays(1), 1);

        ReservationResponse resp = service.reserve(1L, req); // khong throw NPE

        // 1 dem * basePrice 1.000.000
        assertEquals(0, new BigDecimal("1000000").compareTo(resp.totalPrice()));

        // override row van price = null sau khi save (khong snapshot basePrice) - PMS-03
        ArgumentCaptor<RoomInventory> invCap = ArgumentCaptor.forClass(RoomInventory.class);
        verify(invRepo, atLeastOnce()).save(invCap.capture());
        assertEquals(null, invCap.getValue().getPrice());
        assertEquals(4, invCap.getValue().getAvailableRooms()); // 5 - 1
    }

    /** PMS-03: dem chua co override -> tao row moi voi price = null (fallback basePrice sau nay). */
    @Test
    void reserveMaterializesOverrideWithNullPrice() {
        when(rtRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(rt));
        when(invRepo.findByRoomTypeIdAndDate(eq(1L), any())).thenReturn(Optional.empty());
        when(resRepo.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReserveRequest req = new ReserveRequest(1L, "Carol",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 2), 2);

        service.reserve(1L, req);

        ArgumentCaptor<RoomInventory> invCap = ArgumentCaptor.forClass(RoomInventory.class);
        verify(invRepo).save(invCap.capture());
        RoomInventory saved = invCap.getValue();
        assertEquals(null, saved.getPrice());           // PMS-03: khong snapshot gia
        assertEquals(8, saved.getAvailableRooms());      // totalRooms 10 - 2
    }

    /** PMS-05: cancel clamp restore ve min(available + rooms, totalRooms). */
    @Test
    void cancelClampsRestoreToTotalRooms() {
        Reservation r = new Reservation();
        r.setId(99L);
        r.setRoomTypeId(1L);
        r.setRooms(3);
        r.setStatus(ReservationStatus.CONFIRMED_VALUE);
        LocalDate night = LocalDate.of(2026, 7, 1);
        r.setCheckIn(night);
        r.setCheckOut(night.plusDays(1));

        // override bi rebuild/xoa giua chung -> availableRooms da = totalRooms (10)
        RoomInventory inflated = new RoomInventory();
        inflated.setRoomTypeId(1L);
        inflated.setDate(night);
        inflated.setAvailableRooms(10);

        when(resRepo.findById(99L)).thenReturn(Optional.of(r));
        when(rtRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(rt));
        when(invRepo.findByRoomTypeIdAndDate(1L, night)).thenReturn(Optional.of(inflated));
        when(resRepo.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        service.cancel(99L);

        ArgumentCaptor<RoomInventory> invCap = ArgumentCaptor.forClass(RoomInventory.class);
        verify(invRepo).save(invCap.capture());
        // 10 + 3 = 13 nhung clamp ve totalRooms = 10
        assertEquals(10, invCap.getValue().getAvailableRooms());
        assertEquals(ReservationStatus.CANCELLED_VALUE, r.getStatus());
    }

    /** By-code cancel (INT-01): resolve qua findByConfirmationCode roi huy. */
    @Test
    void cancelByCodeResolvesAndCancels() {
        Reservation r = new Reservation();
        r.setId(7L);
        r.setConfirmationCode("HZABC12345");
        r.setRoomTypeId(1L);
        r.setRooms(1);
        r.setStatus(ReservationStatus.CONFIRMED_VALUE);
        LocalDate night = LocalDate.of(2026, 7, 1);
        r.setCheckIn(night);
        r.setCheckOut(night.plusDays(1));

        RoomInventory inv = new RoomInventory();
        inv.setRoomTypeId(1L);
        inv.setDate(night);
        inv.setAvailableRooms(4);

        when(resRepo.findByConfirmationCode("HZABC12345")).thenReturn(Optional.of(r));
        when(rtRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(rt));
        when(invRepo.findByRoomTypeIdAndDate(1L, night)).thenReturn(Optional.of(inv));
        when(resRepo.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse resp = service.cancelByCode("HZABC12345");

        verify(resRepo).findByConfirmationCode("HZABC12345");
        assertEquals(ReservationStatus.CANCELLED_VALUE, resp.status());
        // 4 + 1 = 5, under totalRooms 10 -> 5
        assertEquals(5, inv.getAvailableRooms());
    }

    /** Oversell guard: het phong (available 0) -> reserve nem IllegalStateException, khong tru/save. */
    @Test
    void reserveRejectsWhenSoldOut() {
        RoomInventory soldOut = new RoomInventory();
        soldOut.setRoomTypeId(1L);
        LocalDate night = LocalDate.of(2026, 7, 1);
        soldOut.setDate(night);
        soldOut.setAvailableRooms(0);

        when(rtRepo.findByIdForUpdate(1L)).thenReturn(Optional.of(rt));
        when(invRepo.findByRoomTypeIdAndDate(1L, night)).thenReturn(Optional.of(soldOut));

        ReserveRequest req = new ReserveRequest(1L, "Dave", night, night.plusDays(1), 1);

        try {
            service.reserve(1L, req);
            throw new AssertionError("expected IllegalStateException for sold-out night");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Not enough rooms"));
        }
        verify(rtRepo).findByIdForUpdate(1L); // van lock truoc khi check
        verify(resRepo, never()).save(any(Reservation.class));
    }
}
