package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomUserDetailsService userDetailsService;

    public AuthController(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // По умолчанию регистрируем как USER, если хотим админа - нужна отдельная логика или секретный ключ
            // Для лабы упростим: если в имени есть "admin", даем админа (для теста), иначе USER
            String role = request.getUsername().toLowerCase().contains("admin") ? "ROLE_ADMIN" : "ROLE_USER";

            userDetailsService.registerUser(request.getUsername(), request.getPassword(), role);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}