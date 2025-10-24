package com.example.hotelmanagement.service;

import com.example.hotelmanagement.exception.BookingConflictException;
import com.example.hotelmanagement.model.Booking;
import com.example.hotelmanagement.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public void validateNoOverlap(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> existingBookings = bookingRepository.findByRoomIdAndActiveTrue(roomId);
        for (Booking b : existingBookings) {
            if (!checkOut.isBefore(b.getCheckInDate()) && !checkIn.isAfter(b.getCheckOutDate())) {
                throw new BookingConflictException("Booking overlaps with existing booking for room " + roomId);
            }
        }
    }
}