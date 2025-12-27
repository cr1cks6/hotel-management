package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.service.AuthService;
import com.example.hotelmanagement.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final AuthService authService;

    public AuthController(CustomUserDetailsService userDetailsService, AuthService authService) {
        this.userDetailsService = userDetailsService;
        this.authService = authService;
    }

    // РЕГИСТРАЦИЯ
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String role = request.getUsername().toLowerCase().contains("admin") ? "ROLE_ADMIN" : "ROLE_USER";
            userDetailsService.registerUser(request.getUsername(), request.getPassword(), role);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ЛОГИН (Выдача токенов)
    @PostMapping("/login")
    public AuthService.TokenResponse login(@RequestBody LoginRequest request) {
        try {
            return authService.login(request.getUsername(), request.getPassword());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    // ОБНОВЛЕНИЕ ТОКЕНОВ
    @PostMapping("/refresh")
    public AuthService.TokenResponse refresh(@RequestBody RefreshRequest request) {
        try {
            return authService.refreshToken(request.getRefreshToken());
        } catch (Exception e) {
            // Если токен использован повторно или просрочен -> 403 Forbidden
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    // DTOs
    public static class RegisterRequest {
        @NotBlank private String username;
        @NotBlank private String password;
        // Getters/Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
        // Getters/Setters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }

    public static class RefreshRequest {
        @NotBlank private String refreshToken;
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}