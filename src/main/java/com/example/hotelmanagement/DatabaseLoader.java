package com.example.hotelmanagement;

import com.example.hotelmanagement.model.*;
import com.example.hotelmanagement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DatabaseLoader implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;

    public DatabaseLoader(HotelRepository hotelRepository, RoomRepository roomRepository,
                          GuestRepository guestRepository, BookingRepository bookingRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (hotelRepository.count() == 0) {
            // Создаем отель
            Hotel hotel = new Hotel("Grand Budapest", "Zubrowka");
            hotelRepository.save(hotel);

            // Создаем комнаты
            Room r1 = new Room(101, new BigDecimal("100.00"));
            r1.setHotel(hotel);
            roomRepository.save(r1);

            Room r2 = new Room(102, new BigDecimal("150.00"));
            r2.setHotel(hotel);
            roomRepository.save(r2);

            // Создаем гостя
            Guest guest = new Guest("Ivan Ivanov", "ivan@test.com", "+123456789");
            guestRepository.save(guest);

            // Создаем бронь
            Booking booking = new Booking(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
            booking.setRoom(r1);
            booking.setGuest(guest);
            booking.setActive(true);
            bookingRepository.save(booking);

            System.out.println("--- Database Initialized with Test Data ---");
        }
    }
}