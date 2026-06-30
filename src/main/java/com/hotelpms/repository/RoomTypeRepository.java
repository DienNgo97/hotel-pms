package com.hotelpms.repository;

import com.hotelpms.domain.entity.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelId(Long hotelId);

    /**
     * Lay row RoomType voi PESSIMISTIC_WRITE lock (SELECT ... FOR UPDATE).
     * Dung de serialize reserve()/cancel() cho cung mot room type -> chong oversell (PMS-02).
     * Vi moi dem cua mot reservation deu thuoc cung mot room type, khoa row RoomType la du
     * de chan hai reserve dong thoi cung vuot qua buoc kiem tra ton kho.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rt from RoomType rt where rt.id = :id")
    Optional<RoomType> findByIdForUpdate(@Param("id") Long id);
}
