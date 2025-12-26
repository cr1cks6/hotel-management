package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.model.Booking;
import com.example.hotelmanagement.model.Payment;
import com.example.hotelmanagement.service.HotelService;
import com.example.hotelmanagement.repository.BookingRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final HotelService hotelService;
    private final BookingRepository bookingRepository;

    public BookingController(HotelService hotelService, BookingRepository bookingRepository) {
        this.hotelService = hotelService;
        this.bookingRepository = bookingRepository;
    }

    // =================================================================================
    // НОВЫЕ БИЗНЕС-ОПЕРАЦИИ (Лабораторная 3)
    // =================================================================================

    // --- ОПЕРАЦИЯ 1: Бронь с депозитом ---
    @PostMapping
    public Booking createBookingWithDeposit(@Valid @RequestBody BookingRequest request) {
        try {
            return hotelService.bookWithDeposit(
                    request.getRoomId(),
                    request.getGuestId(),
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // --- ОПЕРАЦИЯ 3: Выселение и доплата ---
    @PostMapping("/{id}/checkout")
    public Payment checkOut(@PathVariable Long id) {
        try {
            return hotelService.checkOutAndPayBalance(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // --- ОПЕРАЦИЯ 4: Переселение ---
    @PostMapping("/{id}/relocate")
    public Booking relocate(@PathVariable Long id, @RequestParam Long newRoomId) {
        try {
            return hotelService.relocateGuest(id, newRoomId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // =================================================================================
    // СТАНДАРТНЫЕ CRUD МЕТОДЫ (Вернул их обратно)
    // =================================================================================

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    public Booking getBooking(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    // ВОТ МЕТОД, КОТОРЫЙ ПРОПАЛ (Удаление)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    // =================================================================================
    // DTO
    // =================================================================================
    public static class BookingRequest {
        @NotNull private Long roomId;
        @NotNull private Long guestId;
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull private LocalDate checkInDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull private LocalDate checkOutDate;

        public Long getRoomId() { return roomId; }
        public Long getGuestId() { return guestId; }
        public LocalDate getCheckInDate() { return checkInDate; }
        public LocalDate getCheckOutDate() { return checkOutDate; }
    }
}