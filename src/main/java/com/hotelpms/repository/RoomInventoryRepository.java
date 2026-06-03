package com.hotelpms.repository;

import com.hotelpms.domain.entity.RoomInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {
    Optional<RoomInventory> findByRoomTypeIdAndDate(Long roomTypeId, LocalDate date);
    List<RoomInventory> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate from, LocalDate to);
}
