package com.hotelpms.api.dto;

import java.math.BigDecimal;

public record RoomTypeDto(
        Long id,
        Long hotelId,
        String name,
        String description,
        int capacity,
        BigDecimal basePrice,
        String currency,
        int totalRooms) {
}
