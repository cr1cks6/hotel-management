package com.example.hotelmanagement.service;

import com.example.hotelmanagement.model.User;
import com.example.hotelmanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Хэш пароля
                .roles(user.getRole().replace("ROLE_", "")) // Spring Security требует роль без префикса здесь
                .build();
    }

    // Метод регистрации с валидацией
    public void registerUser(String username, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Проверка надежности пароля (Лаб 4)
        if (rawPassword.length() < 8 || !rawPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Password must be at least 8 chars long and contain special symbols");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword)); // Хэшируем пароль!
        user.setRole(role);

        userRepository.save(user);
    }
}