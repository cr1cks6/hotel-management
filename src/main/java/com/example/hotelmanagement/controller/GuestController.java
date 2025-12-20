package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.model.Guest;
import com.example.hotelmanagement.repository.GuestRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestRepository guestRepository;

    public GuestController(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @PostMapping
    public Guest createGuest(@Valid @RequestBody Guest guest) {
        return guestRepository.save(guest);
    }

    @GetMapping
    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    @GetMapping("/{id}")
    public Guest getGuest(@PathVariable Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found"));
    }

    @PutMapping("/{id}")
    public Guest updateGuest(@PathVariable Long id, @Valid @RequestBody Guest guestDetails) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found"));
        guest.setFullName(guestDetails.getFullName());
        guest.setEmail(guestDetails.getEmail());
        guest.setPhone(guestDetails.getPhone());
        return guestRepository.save(guest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuest(@PathVariable Long id) {
        if (!guestRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found");
        }
        guestRepository.deleteById(id);
    }
}