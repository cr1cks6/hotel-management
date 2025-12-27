package com.example.hotelmanagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // Не забудь этот импорт!
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    // --- ИСПРАВЛЕНИЕ ОШИБКИ 500 ---
    // Добавляем @JsonIgnore.
    // Это значит: "При удалении брони удаляй платежи (cascade),
    // НО когда показываешь бронь в Postman, не пытайся показывать список платежей".
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Payment> payments = new ArrayList<>();

    public Booking() {}

    public Booking(LocalDate checkInDate, LocalDate checkOutDate) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
}