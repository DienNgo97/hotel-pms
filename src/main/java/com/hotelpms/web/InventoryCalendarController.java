package com.hotelpms.web;

import com.hotelpms.api.dto.InventoryResponse;
import com.hotelpms.api.dto.RoomTypeInventoryDto;
import com.hotelpms.domain.entity.RoomType;
import com.hotelpms.service.HotelService;
import com.hotelpms.service.InventoryService;
import com.hotelpms.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/hotels/{hotelId}/rooms/{roomTypeId}/inventory")
public class InventoryCalendarController {

    private final InventoryService inventoryService;
    private final RoomService roomService;
    private final HotelService hotelService;

    public InventoryCalendarController(InventoryService inventoryService, RoomService roomService,
                                       HotelService hotelService) {
        this.inventoryService = inventoryService;
        this.roomService = roomService;
        this.hotelService = hotelService;
    }

    @GetMapping
    public String calendar(@PathVariable Long hotelId, @PathVariable Long roomTypeId,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                           @RequestParam(required = false, defaultValue = "14") int days,
                           Model model, RedirectAttributes ra) {
        RoomType rt = roomService.findById(roomTypeId).orElse(null);
        if (rt == null) {
            ra.addFlashAttribute("error", "Room type not found");
            return "redirect:/admin/hotels/" + hotelId;
        }
        LocalDate from = start != null ? start : LocalDate.now();
        LocalDate to = from.plusDays(Math.max(days, 1) - 1L);

        InventoryResponse inv = inventoryService.getInventory(hotelId, from, to);
        RoomTypeInventoryDto rtInv = inv.roomTypes().stream()
                .filter(x -> x.roomTypeId().equals(roomTypeId))
                .findFirst().orElse(null);

        model.addAttribute("hotel", hotelService.findById(hotelId).orElse(null));
        model.addAttribute("roomType", rt);
        model.addAttribute("rtInv", rtInv);
        model.addAttribute("from", from);
        model.addAttribute("days", days);
        return "inventory/calendar";
    }

    @PostMapping
    public String update(@PathVariable Long hotelId, @PathVariable Long roomTypeId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         @RequestParam int availableRooms,
                         @RequestParam(required = false) BigDecimal price,
                         RedirectAttributes ra) {
        RoomType rt = roomService.findById(roomTypeId).orElse(null);
        if (rt != null) {
            inventoryService.setDay(rt, date, availableRooms, price);
            ra.addFlashAttribute("message", "Updated inventory for " + date);
        }
        return "redirect:/admin/hotels/" + hotelId + "/rooms/" + roomTypeId + "/inventory";
    }
}
