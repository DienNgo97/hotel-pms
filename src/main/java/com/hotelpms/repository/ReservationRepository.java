package com.hotelpms.repository;

import com.hotelpms.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByConfirmationCode(String confirmationCode);
    List<Reservation> findAllByOrderByCreatedAtDesc();
}
