package com.hotelpms.api.dto;

import java.util.List;

public record RoomTypeInventoryDto(
        Long roomTypeId,
        String roomTypeName,
        List<DayInventoryDto> days) {
}
