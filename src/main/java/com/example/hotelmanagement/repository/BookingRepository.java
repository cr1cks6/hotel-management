package com.example.hotelmanagement.repository;

import com.example.hotelmanagement.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRoomIdAndActiveTrue(Long roomId);
}