package com.example.hotelmanagement.repository;

import com.example.hotelmanagement.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // --- ВЕРНУЛ ЭТОТ МЕТОД (он нужен для твоего кода) ---
    List<Booking> findByRoomIdAndActiveTrue(Long roomId);

    // --- НОВЫЕ МЕТОДЫ ДЛЯ ЛАБ 3 ---

    // Сложный запрос для проверки пересечений дат (бизнес-логика)
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.active = true AND " +
            "(b.checkInDate < :endDate AND b.checkOutDate > :startDate)")
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // Поиск всех бронирований для конкретного отеля (для статистики)
    List<Booking> findByRoomHotelId(Long hotelId);
}