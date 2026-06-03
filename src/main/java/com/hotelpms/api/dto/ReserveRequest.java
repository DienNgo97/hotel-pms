package com.hotelpms.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReserveRequest(
        @NotNull Long roomTypeId,
        @NotBlank String guestName,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @Min(1) int rooms) {
}
