package com.hotelpms.api;

import com.hotelpms.api.dto.HotelDto;
import com.hotelpms.api.dto.InventoryResponse;
import com.hotelpms.api.dto.ReservationResponse;
import com.hotelpms.api.dto.ReserveRequest;
import com.hotelpms.api.dto.RoomTypeDto;
import com.hotelpms.service.HotelService;
import com.hotelpms.service.InventoryService;
import com.hotelpms.service.ReservationService;
import com.hotelpms.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pms/v1")
public class PmsApiController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final InventoryService inventoryService;
    private final ReservationService reservationService;

    public PmsApiController(HotelService hotelService, RoomService roomService,
                            InventoryService inventoryService, ReservationService reservationService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.inventoryService = inventoryService;
        this.reservationService = reservationService;
    }

    @GetMapping("/hotels")
    public List<HotelDto> hotels() {
        return hotelService.findAll().stream().map(hotelService::toDto).toList();
    }

    @GetMapping("/hotels/{id}")
    public ResponseEntity<HotelDto> hotel(@PathVariable Long id) {
        return hotelService.findById(id)
                .map(h -> ResponseEntity.ok(hotelService.toDto(h)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hotels/{id}/rooms")
    public ResponseEntity<List<RoomTypeDto>> rooms(@PathVariable Long id) {
        if (hotelService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roomService.findByHotel(id).stream().map(roomService::toDto).toList());
    }

    @GetMapping("/hotels/{id}/inventory")
    public ResponseEntity<InventoryResponse> inventory(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (hotelService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inventoryService.getInventory(id, from, to));
    }

    @PostMapping("/hotels/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable Long id, @Valid @RequestBody ReserveRequest req) {
        try {
            return ResponseEntity.ok(reservationService.reserve(id, req));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try {
            ReservationResponse resp = reservationService.cancel(id);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Huy reservation theo confirmationCode (INT-01). Booking-platform chi luu confirmationCode
     * (HZ...) nen can duong nay de huy end-to-end. Endpoint theo {id} ben tren van giu nguyen.
     */
    @PostMapping("/reservations/by-code/{confirmationCode}/cancel")
    public ResponseEntity<?> cancelByCode(@PathVariable String confirmationCode) {
        try {
            ReservationResponse resp = reservationService.cancelByCode(confirmationCode);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
        }
    }
}
