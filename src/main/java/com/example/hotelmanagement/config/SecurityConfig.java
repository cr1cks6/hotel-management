package com.example.hotelmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. РЕГИСТРАЦИЯ (Доступно всем)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. ГЛОБАЛЬНЫЙ ЗАПРЕТ НА УДАЛЕНИЕ И ИЗМЕНЕНИЕ ДЛЯ ЮЗЕРОВ
                        // Любой DELETE запрос может делать только АДМИН
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        // Любой PUT (Обновление) запрос может делать только АДМИН
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")

                        // 3. АДМИНСКИЕ ЗАДАЧИ (Создание сущностей)
                        // Отели, Комнаты, Гости, Платежи - создавать может только АДМИН
                        .requestMatchers(HttpMethod.POST, "/api/hotels/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/guests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/**").hasRole("ADMIN")

                        // 4. БИЗНЕС-ОПЕРАЦИИ (Выселение и Переселение)
                        // Ты просил запретить юзеру "изменять". Выселение и переселение меняют бронь.
                        // Поэтому разрешаем их только АДМИНУ.
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/checkout").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/relocate").hasRole("ADMIN")

                        // 5. ЕДИНСТВЕННОЕ РАЗРЕШЕНИЕ ДЛЯ ЮЗЕРА (кроме просмотра)
                        // Создание новой брони доступно ВСЕМ авторизованным
                        .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()

                        // 6. ПРОСМОТР (GET)
                        // Разрешен всем авторизованным пользователям
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

                        // Все остальное закрываем (на всякий случай)
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}