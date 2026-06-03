package com.hotelpms.web;

import com.hotelpms.repository.HotelRepository;
import com.hotelpms.repository.ReservationRepository;
import com.hotelpms.repository.RoomTypeRepository;
import com.hotelpms.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public DashboardController(HotelRepository hotelRepository, RoomTypeRepository roomTypeRepository,
                               ReservationRepository reservationRepository, ReservationService reservationService) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/admin";
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        long checkInsToday = reservationService.findAll().stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()) && today.equals(r.getCheckIn()))
                .count();
        model.addAttribute("hotelCount", hotelRepository.count());
        model.addAttribute("roomTypeCount", roomTypeRepository.count());
        model.addAttribute("reservationCount", reservationRepository.count());
        model.addAttribute("checkInsToday", checkInsToday);
        return "dashboard";
    }
}
