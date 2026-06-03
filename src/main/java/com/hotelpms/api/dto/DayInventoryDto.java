package com.hotelpms.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DayInventoryDto(
        LocalDate date,
        int availableRooms,
        BigDecimal price) {
}
