package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.model.Hotel;
import com.example.hotelmanagement.model.Room;
import com.example.hotelmanagement.repository.HotelRepository;
import com.example.hotelmanagement.repository.RoomRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository; // ← добавили зависимость

    public RoomController(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    // DTO для входных данных
    public static class RoomRequest {
        private Integer roomNumber;
        private BigDecimal pricePerNight;
        private Long hotelId; // ← принимаем ID отеля

        // Геттеры и сеттеры
        public Integer getRoomNumber() { return roomNumber; }
        public void setRoomNumber(Integer roomNumber) { this.roomNumber = roomNumber; }
        public BigDecimal getPricePerNight() { return pricePerNight; }
        public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
        public Long getHotelId() { return hotelId; }
        public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    }

    @PostMapping
    public Room createRoom(@Valid @RequestBody RoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        Room room = new Room(request.getRoomNumber(), request.getPricePerNight());
        room.setHotel(hotel); // ← ВАЖНО: устанавливаем связь
        return roomRepository.save(room);
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @GetMapping("/{id}")
    public Room getRoom(@PathVariable Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        room.setRoomNumber(request.getRoomNumber());
        room.setPricePerNight(request.getPricePerNight());
        room.setHotel(hotel); // ← обновляем отель
        return roomRepository.save(room);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        roomRepository.deleteById(id);
    }
}