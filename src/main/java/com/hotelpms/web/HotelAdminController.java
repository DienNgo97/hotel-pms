package com.hotelpms.web;

import com.hotelpms.domain.entity.Hotel;
import com.hotelpms.service.HotelService;
import com.hotelpms.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hotels")
public class HotelAdminController {

    private final HotelService hotelService;
    private final RoomService roomService;

    public HotelAdminController(HotelService hotelService, RoomService roomService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hotels", hotelService.findAll());
        return "hotels/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("hotel", new Hotel());
        return "hotels/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return hotelService.findById(id)
                .map(h -> { model.addAttribute("hotel", h); return "hotels/form"; })
                .orElseGet(() -> { ra.addFlashAttribute("error", "Hotel not found"); return "redirect:/admin/hotels"; });
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Hotel hotel, RedirectAttributes ra) {
        hotelService.save(hotel);
        ra.addFlashAttribute("message", "Saved hotel: " + hotel.getName());
        return "redirect:/admin/hotels";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return hotelService.findById(id)
                .map(h -> {
                    model.addAttribute("hotel", h);
                    model.addAttribute("rooms", roomService.findByHotel(id));
                    return "hotels/detail";
                })
                .orElseGet(() -> { ra.addFlashAttribute("error", "Hotel not found"); return "redirect:/admin/hotels"; });
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        hotelService.delete(id);
        ra.addFlashAttribute("message", "Deleted hotel #" + id);
        return "redirect:/admin/hotels";
    }
}
