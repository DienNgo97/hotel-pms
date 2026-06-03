package com.hotelpms.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Override availability/price cho 1 room type vao 1 ngay cu the.
 * Neu khong co row cho mot ngay -> dung default tu RoomType (totalRooms, basePrice).
 */
@Entity
@Table(name = "room_inventory",
       uniqueConstraints = @UniqueConstraint(name = "uk_inv_roomtype_date", columnNames = {"room_type_id", "inv_date"}))
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "inv_date", nullable = false)
    private LocalDate date;

    @Column(name = "available_rooms", nullable = false)
    private int availableRooms;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
