package com.college.shinecart.config;

import com.college.shinecart.entity.User;
import com.college.shinecart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {

            // Only creates admin if it doesn't already exist
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin@123"))
                        .phone("8799959634")
                        .roles(List.of("ROLE_ADMIN"))
                        .enabled(true)
                        .build();

                userRepository.save(admin);
                log.info("Admin user created successfully!");
            } else {
                log.info("Admin user already exists, skipping...");
            }
        };
    }
}