package com.hotelpms.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationResponse(
        Long reservationId,
        String confirmationCode,
        Long hotelId,
        Long roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int rooms,
        BigDecimal totalPrice,
        String currency,
        String status) {
}
