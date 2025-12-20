package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.exception.BookingConflictException;
import com.example.hotelmanagement.model.Booking;
import com.example.hotelmanagement.repository.BookingRepository;
import com.example.hotelmanagement.repository.RoomRepository;
import com.example.hotelmanagement.repository.GuestRepository;
import com.example.hotelmanagement.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    private final BookingService bookingService;

    public BookingController(BookingRepository bookingRepository,
                             RoomRepository roomRepository,
                             GuestRepository guestRepository,
                             BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking createBooking(@Valid @RequestBody BookingRequest request) {
        var room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        var guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found"));

        try {
            bookingService.validateNoOverlap(room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        } catch (BookingConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        Booking booking = new Booking(request.getCheckInDate(), request.getCheckOutDate());
        booking.setRoom(room);
        booking.setGuest(guest);
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
    public Booking updateBooking(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        var room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        var guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found"));

        try {
            bookingService.validateNoOverlap(room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        } catch (BookingConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

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

    public static class BookingRequest {
        private Long roomId;
        private Long guestId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate checkInDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate checkOutDate;

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