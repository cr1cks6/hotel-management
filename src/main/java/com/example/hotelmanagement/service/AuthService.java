package com.example.hotelmanagement.service;

import com.example.hotelmanagement.model.SessionStatus;
import com.example.hotelmanagement.model.User;
import com.example.hotelmanagement.model.UserSession;
import com.example.hotelmanagement.repository.UserRepository;
import com.example.hotelmanagement.repository.UserSessionRepository;
import com.example.hotelmanagement.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, UserSessionRepository userSessionRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // ЛОГИН
    @Transactional
    public TokenResponse login(String username, String password) {
        // 1. Проверяем пользователя
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 2. Генерируем пару токенов
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 3. Сохраняем сессию в БД
        UserSession session = new UserSession(
                user.getUsername(),
                refreshToken,
                Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenValidity()),
                SessionStatus.ACTIVE
        );
        userSessionRepository.save(session);

        return new TokenResponse(accessToken, refreshToken);
    }

    // ОБНОВЛЕНИЕ ТОКЕНОВ (Refresh)
    @Transactional
    public TokenResponse refreshToken(String oldRefreshToken) {
        // 1. Валидируем структуру JWT (подпись, срок действия самого токена)
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new RuntimeException("Invalid refresh token format or expired");
        }

        // 2. Ищем сессию в БД
        UserSession session = userSessionRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 3. Проверка на повторное использование (Replay Attack)
        if (session.getStatus() == SessionStatus.USED) {
            // В идеале тут надо банить пользователя или отзывать все токены
            session.setStatus(SessionStatus.REVOKED);
            userSessionRepository.save(session);
            throw new RuntimeException("Refresh token was already used! Security alert!");
        }

        if (session.getStatus() == SessionStatus.REVOKED) {
            throw new RuntimeException("Session is revoked");
        }

        // 4. Проверяем срок действия в БД (на всякий случай)
        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired in DB");
        }

        // 5. Ротация токенов
        // Старый помечаем как USED
        session.setStatus(SessionStatus.USED);
        userSessionRepository.save(session);

        // Генерируем новые
        User user = userRepository.findByUsername(session.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // Создаем НОВУЮ сессию
        UserSession newSession = new UserSession(
                user.getUsername(),
                newRefreshToken,
                Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenValidity()),
                SessionStatus.ACTIVE
        );
        userSessionRepository.save(newSession);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // DTO для ответа
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;

        public TokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
        // Геттеры
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}