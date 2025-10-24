package com.example.hotelmanagement.repository;

import com.example.hotelmanagement.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {}