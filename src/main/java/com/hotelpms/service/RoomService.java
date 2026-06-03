package com.hotelpms.service;

import com.hotelpms.api.dto.RoomTypeDto;
import com.hotelpms.domain.entity.RoomType;
import com.hotelpms.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomTypeRepository roomTypeRepository;

    public RoomService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    public List<RoomType> findByHotel(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }

    public Optional<RoomType> findById(Long id) {
        return roomTypeRepository.findById(id);
    }

    @Transactional
    public RoomType save(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @Transactional
    public void delete(Long id) {
        roomTypeRepository.deleteById(id);
    }

    public RoomTypeDto toDto(RoomType r) {
        return new RoomTypeDto(r.getId(), r.getHotelId(), r.getName(), r.getDescription(),
                r.getCapacity(), r.getBasePrice(), r.getCurrency(), r.getTotalRooms());
    }
}
