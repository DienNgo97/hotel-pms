package com.hotelpms.api.dto;

public record HotelDto(
        Long id,
        String name,
        String city,
        String address,
        String description,
        Integer starRating,
        boolean active) {
}
