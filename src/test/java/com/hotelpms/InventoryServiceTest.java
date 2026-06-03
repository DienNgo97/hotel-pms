package com.hotelpms;

import com.hotelpms.api.dto.InventoryResponse;
import com.hotelpms.domain.entity.RoomType;
import com.hotelpms.repository.RoomInventoryRepository;
import com.hotelpms.repository.RoomTypeRepository;
import com.hotelpms.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryServiceTest {

    @Test
    void inventoryUsesDefaultsWhenNoOverride() {
        RoomTypeRepository rtRepo = mock(RoomTypeRepository.class);
        RoomInventoryRepository invRepo = mock(RoomInventoryRepository.class);

        RoomType rt = new RoomType();
        rt.setId(1L);
        rt.setHotelId(1L);
        rt.setName("Deluxe");
        rt.setTotalRooms(10);
        rt.setBasePrice(new BigDecimal("1000000"));
        rt.setCurrency("VND");

        when(rtRepo.findByHotelId(1L)).thenReturn(List.of(rt));
        when(invRepo.findByRoomTypeIdAndDateBetween(eq(1L), any(), any())).thenReturn(List.of());

        InventoryService service = new InventoryService(rtRepo, invRepo);
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 3);

        InventoryResponse resp = service.getInventory(1L, from, to);

        assertEquals(1, resp.roomTypes().size());
        assertEquals(3, resp.roomTypes().get(0).days().size());          // 3 ngay
        assertEquals(10, resp.roomTypes().get(0).days().get(0).availableRooms()); // default totalRooms
    }
}
