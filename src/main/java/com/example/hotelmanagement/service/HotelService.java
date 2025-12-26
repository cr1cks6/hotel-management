package com.example.hotelmanagement.service;

import com.example.hotelmanagement.exception.BookingConflictException;
import com.example.hotelmanagement.model.*;
import com.example.hotelmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;

    public HotelService(RoomRepository roomRepository, BookingRepository bookingRepository,
                        GuestRepository guestRepository, PaymentRepository paymentRepository,
                        HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
        this.paymentRepository = paymentRepository;
        this.hotelRepository = hotelRepository;
    }

    // --- ОПЕРАЦИЯ 1: Бронирование с депозитом (Booking + Room + Payment) ---
    @Transactional
    public Booking bookWithDeposit(Long roomId, Long guestId, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) throw new IllegalArgumentException("Check-out must be after check-in");

        // 1. Проверка доступности
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut);
        if (!overlaps.isEmpty()) {
            throw new BookingConflictException("Room is not available for these dates");
        }

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        Guest guest = guestRepository.findById(guestId).orElseThrow(() -> new RuntimeException("Guest not found"));

        // 2. Создаем бронь
        Booking booking = new Booking(checkIn, checkOut);
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setActive(true);
        Booking savedBooking = bookingRepository.save(booking);

        // 3. Считаем стоимость и берем 50% депозит
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days == 0) days = 1;
        BigDecimal totalCost = room.getPricePerNight().multiply(BigDecimal.valueOf(days));
        BigDecimal deposit = totalCost.divide(BigDecimal.valueOf(2));

        // 4. Автоматически создаем платеж (Депозит)
        Payment payment = new Payment(deposit);
        payment.setBooking(savedBooking);
        paymentRepository.save(payment);

        return savedBooking;
    }

    // --- ОПЕРАЦИЯ 2: Поиск свободных (Room + Booking) ---
    public List<Room> findAvailableRooms(Long hotelId, LocalDate from, LocalDate to) {
        return roomRepository.findAvailableRooms(hotelId, from, to);
    }

    // --- ОПЕРАЦИЯ 3: Выселение и доплата (Booking + Payment) ---
    @Transactional
    public Payment checkOutAndPayBalance(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.isActive()) {
            throw new IllegalStateException("Booking is already closed");
        }

        // 1. Считаем полную стоимость
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        if (days == 0) days = 1;
        BigDecimal totalCost = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(days));

        // 2. Считаем, сколько уже было оплачено (ищем все платежи по этой брони)
        List<Payment> existingPayments = paymentRepository.findAllByBookingId(bookingId);
        BigDecimal paidAmount = existingPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Вычисляем остаток
        BigDecimal balance = totalCost.subtract(paidAmount);

        Payment finalPayment = null;
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            // Создаем платеж на остаток
            finalPayment = new Payment(balance);
            finalPayment.setBooking(booking);
            paymentRepository.save(finalPayment);
        }

        // 4. Закрываем бронь
        booking.setActive(false);
        bookingRepository.save(booking);

        return finalPayment != null ? finalPayment : new Payment(BigDecimal.ZERO);
    }

    // --- ОПЕРАЦИЯ 4: Переселение гостя (Booking + Room) ---
    @Transactional
    public Booking relocateGuest(Long bookingId, Long newRoomId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Проверяем занятость новой комнаты
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(newRoomId, booking.getCheckInDate(), booking.getCheckOutDate());
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("New room is occupied");
        }

        Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new RuntimeException("New Room not found"));

        // Меняем комнату
        booking.setRoom(newRoom);
        return bookingRepository.save(booking);
    }

    // --- ОПЕРАЦИЯ 5: Финансовый отчет (Hotel -> Room -> Booking -> Payment) ---
    public Map<String, Object> getHotelFinancialReport(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // Находим все бронирования этого отеля
        List<Booking> bookings = bookingRepository.findByRoomHotelId(hotelId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int transactionsCount = 0;

        for (Booking booking : bookings) {
            // Для каждой брони ищем платежи
            List<Payment> payments = paymentRepository.findAllByBookingId(booking.getId());
            for (Payment p : payments) {
                totalRevenue = totalRevenue.add(p.getAmount());
                transactionsCount++;
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("hotelName", hotel.getName());
        report.put("totalRevenue", totalRevenue);
        report.put("totalTransactions", transactionsCount);
        report.put("bookingsCount", bookings.size());

        return report;
    }
}