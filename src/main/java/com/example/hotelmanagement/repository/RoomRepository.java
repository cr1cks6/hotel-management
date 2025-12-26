package com.example.hotelmanagement.repository;

import com.example.hotelmanagement.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // Бизнес-операция: Поиск свободных комнат
    // Выбираем комнаты, id которых НЕТ в списке забронированных на эти даты
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.id NOT IN (" +
            "SELECT b.room.id FROM Booking b WHERE " +
            "b.active = true AND " +
            "(b.checkInDate < :endDate AND b.checkOutDate > :startDate))")
    List<Room> findAvailableRooms(@Param("hotelId") Long hotelId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    List<Room> findByHotelId(Long hotelId);
}