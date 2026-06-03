package com.hotelpms.web;

import com.hotelpms.domain.entity.RoomType;
import com.hotelpms.service.HotelService;
import com.hotelpms.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hotels/{hotelId}/rooms")
public class RoomAdminController {

    private final RoomService roomService;
    private final HotelService hotelService;

    public RoomAdminController(RoomService roomService, HotelService hotelService) {
        this.roomService = roomService;
        this.hotelService = hotelService;
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long hotelId, Model model) {
        RoomType rt = new RoomType();
        rt.setHotelId(hotelId);
        model.addAttribute("roomType", rt);
        model.addAttribute("hotelId", hotelId);
        return "rooms/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long hotelId, @PathVariable Long id, Model model, RedirectAttributes ra) {
        return roomService.findById(id)
                .map(rt -> {
                    model.addAttribute("roomType", rt);
                    model.addAttribute("hotelId", hotelId);
                    return "rooms/form";
                })
                .orElseGet(() -> { ra.addFlashAttribute("error", "Room type not found"); return "redirect:/admin/hotels/" + hotelId; });
    }

    @PostMapping("/save")
    public String save(@PathVariable Long hotelId, @ModelAttribute RoomType roomType, RedirectAttributes ra) {
        roomType.setHotelId(hotelId);
        roomService.save(roomType);
        ra.addFlashAttribute("message", "Saved room type: " + roomType.getName());
        return "redirect:/admin/hotels/" + hotelId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long hotelId, @PathVariable Long id, RedirectAttributes ra) {
        roomService.delete(id);
        ra.addFlashAttribute("message", "Deleted room type #" + id);
        return "redirect:/admin/hotels/" + hotelId;
    }
}
