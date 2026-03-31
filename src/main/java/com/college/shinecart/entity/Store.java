package com.college.shinecart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;  // e.g., "ShineCart Mumbai MG Road"

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String phone;

    private String email;

    // Store timings
    private String openingHours;  // e.g., "10:00 AM - 8:00 PM"

    private String workingDays;   // e.g., "Mon-Sat"

    // Location coordinates (optional, for map display)
    private Double latitude;
    private Double longitude;

    private Boolean active = true;  // Is store accepting pickups?

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}