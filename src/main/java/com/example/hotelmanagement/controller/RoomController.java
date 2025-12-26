package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.model.Hotel;
import com.example.hotelmanagement.model.Room;
import com.example.hotelmanagement.service.HotelService;
import com.example.hotelmanagement.repository.RoomRepository;
import com.example.hotelmanagement.repository.HotelRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final HotelService hotelService;

    public RoomController(RoomRepository roomRepository, HotelRepository hotelRepository, HotelService hotelService) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.hotelService = hotelService;
    }

    // ==========================================
    // НОВАЯ БИЗНЕС-ОПЕРАЦИЯ (№2)
    // ==========================================
    @GetMapping("/available")
    public List<Room> getAvailableRooms(@RequestParam Long hotelId,
                                        @RequestParam LocalDate from,
                                        @RequestParam LocalDate to) {
        return hotelService.findAvailableRooms(hotelId, from, to);
    }

    // ==========================================
    // СТАРЫЕ CRUD МЕТОДЫ (Создание, Чтение, Обновление, УДАЛЕНИЕ)
    // ==========================================

    @PostMapping
    public Room createRoom(@Valid @RequestBody RoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));
        Room room = new Room(request.getRoomNumber(), request.getPricePerNight());
        room.setHotel(hotel);
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
        room.setHotel(hotel);
        return roomRepository.save(room);
    }

    // ВОТ МЕТОД, КОТОРЫЙ ПРОПАЛ
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        roomRepository.deleteById(id);
    }

    // DTO
    public static class RoomRequest {
        private Integer roomNumber;
        private BigDecimal pricePerNight;
        private Long hotelId;

        public Integer getRoomNumber() { return roomNumber; }
        public void setRoomNumber(Integer roomNumber) { this.roomNumber = roomNumber; }
        public BigDecimal getPricePerNight() { return pricePerNight; }
        public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
        public Long getHotelId() { return hotelId; }
        public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    }
}