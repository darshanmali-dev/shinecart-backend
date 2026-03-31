package com.college.shinecart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tracking_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String status;  // Order Placed, Processing, Shipped, In Transit, Delivered

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String time;  // e.g., "10:30 AM" or "Expected by 6:00 PM"

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean completed;

    private Boolean current;  // True if this is the current step

    private String icon;  // CheckCircle, Package, Truck, MapPin

    @CreationTimestamp
    private LocalDateTime timestamp;  // Auto timestamp for sorting
}