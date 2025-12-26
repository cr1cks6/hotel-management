package com.example.hotelmanagement.repository;

import com.example.hotelmanagement.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Найти все платежи по ID бронирования
    List<Payment> findAllByBookingId(Long bookingId);
}