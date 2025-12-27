package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.model.Booking;
import com.example.hotelmanagement.model.Payment;
import com.example.hotelmanagement.repository.BookingRepository;
import com.example.hotelmanagement.repository.PaymentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentController(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @PostMapping
    public Payment createPayment(@Valid @RequestBody PaymentRequest request) {
        // Ищем бронь. Если нет - 404 Not Found
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found with id: " + request.getBookingId()));

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setBooking(booking);

        return paymentRepository.save(payment);
    }

    // DTO для запроса
    public static class PaymentRequest {
        @NotNull
        private BigDecimal amount;
        @NotNull
        private Long bookingId;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    }
}