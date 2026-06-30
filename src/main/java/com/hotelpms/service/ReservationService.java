package com.hotelpms.service;

import com.hotelpms.api.dto.ReservationResponse;
import com.hotelpms.api.dto.ReserveRequest;
import com.hotelpms.domain.ReservationStatus;
import com.hotelpms.domain.entity.Reservation;
import com.hotelpms.domain.entity.RoomInventory;
import com.hotelpms.domain.entity.RoomType;
import com.hotelpms.repository.ReservationRepository;
import com.hotelpms.repository.RoomInventoryRepository;
import com.hotelpms.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(RoomTypeRepository roomTypeRepository,
                              RoomInventoryRepository inventoryRepository,
                              ReservationRepository reservationRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public ReservationResponse reserve(Long hotelId, ReserveRequest req) {
        // PMS-02: lay RoomType voi PESSIMISTIC_WRITE lock NGAY DAU transaction de serialize
        // cac reserve dong thoi cho cung room type -> chong oversell (check-then-decrement race).
        RoomType rt = roomTypeRepository.findByIdForUpdate(req.roomTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Room type not found: " + req.roomTypeId()));
        if (!rt.getHotelId().equals(hotelId)) {
            throw new IllegalArgumentException("Room type does not belong to hotel " + hotelId);
        }
        if (!req.checkOut().isAfter(req.checkIn())) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        int rooms = Math.max(req.rooms(), 1);

        // Kiem tra du phong moi dem, dong thoi tinh tong tien
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate d = req.checkIn(); d.isBefore(req.checkOut()); d = d.plusDays(1)) {
            RoomInventory inv = getOrDefault(rt, d);
            if (inv.getAvailableRooms() < rooms) {
                throw new IllegalStateException("Not enough rooms on " + d);
            }
            // PMS-04: override row co the co price = null -> fallback ve basePrice hien tai
            // (giong InventoryService.getInventory/setDay) de tranh NPE.
            BigDecimal nightly = inv.getPrice() != null ? inv.getPrice() : rt.getBasePrice();
            total = total.add(nightly.multiply(BigDecimal.valueOf(rooms)));
        }

        // Tru phong moi dem
        for (LocalDate d = req.checkIn(); d.isBefore(req.checkOut()); d = d.plusDays(1)) {
            RoomInventory inv = getOrDefault(rt, d);
            inv.setAvailableRooms(inv.getAvailableRooms() - rooms);
            inventoryRepository.save(inv);
        }

        Reservation r = new Reservation();
        r.setConfirmationCode("HZ" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        r.setHotelId(hotelId);
        r.setRoomTypeId(rt.getId());
        r.setGuestName(req.guestName());
        r.setCheckIn(req.checkIn());
        r.setCheckOut(req.checkOut());
        r.setRooms(rooms);
        r.setTotalPrice(total);
        r.setStatus(ReservationStatus.CONFIRMED_VALUE);
        reservationRepository.save(r);

        return toResponse(r, rt.getCurrency());
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        return doCancel(r);
    }

    /**
     * Huy reservation theo confirmationCode (HZ...). Ho tro INT-01: booking-platform chi luu
     * confirmationCode nen can mot duong huy theo code. Dung chung logic huy voi cancel(id).
     */
    @Transactional
    public ReservationResponse cancelByCode(String confirmationCode) {
        Reservation r = reservationRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + confirmationCode));
        return doCancel(r);
    }

    private ReservationResponse doCancel(Reservation r) {
        String currency = "VND";
        // PMS-02: khoa RoomType row truoc khi restore ton kho de serialize voi reserve/cancel khac.
        RoomType rt = roomTypeRepository.findByIdForUpdate(r.getRoomTypeId()).orElse(null);
        if (rt != null) currency = rt.getCurrency();

        if (!ReservationStatus.CANCELLED_VALUE.equals(r.getStatus()) && rt != null) {
            int totalRooms = rt.getTotalRooms();
            for (LocalDate d = r.getCheckIn(); d.isBefore(r.getCheckOut()); d = d.plusDays(1)) {
                RoomInventory inv = getOrDefault(rt, d);
                // PMS-05: clamp ve min(available + rooms, totalRooms) de mot override bi xoa/dung lai
                // o giua khong the thoi availability vuot suc chua.
                int restored = Math.min(inv.getAvailableRooms() + r.getRooms(), totalRooms);
                inv.setAvailableRooms(restored);
                inventoryRepository.save(inv);
            }
            r.setStatus(ReservationStatus.CANCELLED_VALUE);
            reservationRepository.save(r);
        }
        return toResponse(r, currency);
    }

    /**
     * Lay row inventory cua ngay; neu chua co thi tao tu default cua room type (chua save).
     * PMS-03: KHONG snapshot basePrice vao price khi materialize override row moi -> de price = null
     * de dem do tiep tuc fallback ve RoomType.basePrice hien tai (tranh dong bang gia cu).
     * Chi set availableRooms.
     */
    private RoomInventory getOrDefault(RoomType rt, LocalDate date) {
        return inventoryRepository.findByRoomTypeIdAndDate(rt.getId(), date)
                .orElseGet(() -> {
                    RoomInventory inv = new RoomInventory();
                    inv.setRoomTypeId(rt.getId());
                    inv.setDate(date);
                    inv.setAvailableRooms(rt.getTotalRooms());
                    // price co tinh giu null -> fallback basePrice (PMS-03/PMS-04)
                    return inv;
                });
    }

    private ReservationResponse toResponse(Reservation r, String currency) {
        return new ReservationResponse(
                r.getId(), r.getConfirmationCode(), r.getHotelId(), r.getRoomTypeId(),
                r.getCheckIn(), r.getCheckOut(), r.getRooms(), r.getTotalPrice(), currency, r.getStatus());
    }
}
