package com.hotelpms.service;

import com.hotelpms.api.dto.DayInventoryDto;
import com.hotelpms.api.dto.InventoryResponse;
import com.hotelpms.api.dto.RoomTypeInventoryDto;
import com.hotelpms.domain.entity.RoomInventory;
import com.hotelpms.domain.entity.RoomType;
import com.hotelpms.repository.RoomInventoryRepository;
import com.hotelpms.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository inventoryRepository;

    public InventoryService(RoomTypeRepository roomTypeRepository,
                            RoomInventoryRepository inventoryRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Tra inventory cho moi room type cua hotel, tung ngay trong [from, to].
     * Ngay khong co override -> dung default (totalRooms, basePrice) cua room type.
     */
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long hotelId, LocalDate from, LocalDate to) {
        List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);
        List<RoomTypeInventoryDto> result = new ArrayList<>();

        for (RoomType rt : roomTypes) {
            Map<LocalDate, RoomInventory> overrides = new HashMap<>();
            for (RoomInventory inv : inventoryRepository.findByRoomTypeIdAndDateBetween(rt.getId(), from, to)) {
                overrides.put(inv.getDate(), inv);
            }
            List<DayInventoryDto> days = new ArrayList<>();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                RoomInventory ov = overrides.get(d);
                int avail = ov != null ? ov.getAvailableRooms() : rt.getTotalRooms();
                BigDecimal price = (ov != null && ov.getPrice() != null) ? ov.getPrice() : rt.getBasePrice();
                days.add(new DayInventoryDto(d, avail, price));
            }
            result.add(new RoomTypeInventoryDto(rt.getId(), rt.getName(), days));
        }
        return new InventoryResponse(hotelId, from, to, result);
    }

    /** Lay so phong trong thuc te cua 1 room type vao 1 ngay (override hoac default). */
    @Transactional(readOnly = true)
    public int availableOn(RoomType rt, LocalDate date) {
        return inventoryRepository.findByRoomTypeIdAndDate(rt.getId(), date)
                .map(RoomInventory::getAvailableRooms)
                .orElse(rt.getTotalRooms());
    }

    /** Operator set availability/price cho 1 ngay (admin inventory calendar). */
    @Transactional
    public void setDay(RoomType rt, LocalDate date, int availableRooms, BigDecimal price) {
        RoomInventory inv = inventoryRepository.findByRoomTypeIdAndDate(rt.getId(), date)
                .orElseGet(() -> {
                    RoomInventory r = new RoomInventory();
                    r.setRoomTypeId(rt.getId());
                    r.setDate(date);
                    return r;
                });
        inv.setAvailableRooms(availableRooms);
        inv.setPrice(price != null ? price : rt.getBasePrice());
        inventoryRepository.save(inv);
    }
}
