package com.example.hotelmanagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // Важный импорт
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    private LocalDateTime paymentDate = LocalDateTime.now();

    // Связь с бронированием
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore // <--- ЭТА СТРОКА ОБЯЗАТЕЛЬНА! Она предотвращает ошибку 500
    private Booking booking;

    public Payment() {}

    public Payment(BigDecimal amount) {
        this.amount = amount;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}