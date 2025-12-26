package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.exception.BookingConflictException;
import com.example.hotelmanagement.model.Booking;
import com.example.hotelmanagement.repository.BookingRepository;
import com.example.hotelmanagement.repository.RoomRepository;
import com.example.hotelmanagement.repository.GuestRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    public BookingController(BookingRepository bookingRepository,
                             RoomRepository roomRepository,
                             GuestRepository guestRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
    }

    @PostMapping
    public Booking createBooking(@Valid @RequestBody BookingRequest request) {
        var room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        var guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found"));

        validateNoOverlap(null, room.getId(), request.getCheckInDate(), request.getCheckOutDate());

        Booking booking = new Booking();
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setActive(true);
        return bookingRepository.save(booking);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    public Booking getBooking(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    @PutMapping("/{id}")
    @Transactional
    public Booking updateBooking(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        var room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        var guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found"));

        validateNoOverlap(id, room.getId(), request.getCheckInDate(), request.getCheckOutDate());

        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setRoom(room);
        booking.setGuest(guest);
        return bookingRepository.save(booking);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    private void validateNoOverlap(Long excludeBookingId, Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates must not be null");
        }

        List<Booking> existingBookings = bookingRepository.findByRoomIdAndActiveTrue(roomId);
        for (Booking b : existingBookings) {
            if (excludeBookingId != null && b.getId().equals(excludeBookingId)) {
                continue;
            }

            // Защита от null в существующих бронях
            if (b.getCheckInDate() == null || b.getCheckOutDate() == null) {
                continue; // или можно выбросить ошибку — зависит от политики
            }

            // Пересечение: если новые даты НЕ полностью до или после старых
            if (!checkOut.isBefore(b.getCheckInDate()) && !checkIn.isAfter(b.getCheckOutDate())) {
                throw new BookingConflictException(
                        "Booking overlaps with existing booking (ID: " + b.getId() + ") for room " + roomId
                );
            }
        }
    }

    public static class BookingRequest {
        @NotNull(message = "roomId is required")
        private Long roomId;

        @NotNull(message = "guestId is required")
        private Long guestId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "checkInDate is required")
        private LocalDate checkInDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "checkOutDate is required")
        private LocalDate checkOutDate;

        // Геттеры и сеттеры
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        public Long getGuestId() { return guestId; }
        public void setGuestId(Long guestId) { this.guestId = guestId; }
        public LocalDate getCheckInDate() { return checkInDate; }
        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
        public LocalDate getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    }
}