package com.hotelpms.api.dto;

import java.util.List;

public record InventoryResponse(
        Long hotelId,
        java.time.LocalDate from,
        java.time.LocalDate to,
        List<RoomTypeInventoryDto> roomTypes) {
}
