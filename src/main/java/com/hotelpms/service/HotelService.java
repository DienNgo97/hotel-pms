package com.hotelpms.service;

import com.hotelpms.api.dto.HotelDto;
import com.hotelpms.domain.entity.Hotel;
import com.hotelpms.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> findById(Long id) {
        return hotelRepository.findById(id);
    }

    @Transactional
    public Hotel save(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Transactional
    public void delete(Long id) {
        hotelRepository.deleteById(id);
    }

    public HotelDto toDto(Hotel h) {
        return new HotelDto(h.getId(), h.getName(), h.getCity(), h.getAddress(),
                h.getDescription(), h.getStarRating(), h.isActive());
    }
}
