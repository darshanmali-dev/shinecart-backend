package com.college.shinecart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;  // e.g., SC240120001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String status;  // Order Placed, Processing, Shipped, In Transit, Delivered

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate expectedDelivery;

    @Column(nullable = false)
    private Double total;

    // Delivery Type: HOME_DELIVERY or STORE_PICKUP
    @Column(nullable = false)
    private String deliveryType;  // HOME_DELIVERY, STORE_PICKUP

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Embedded
    private ShippingAddress shippingAddress;  // Only for HOME_DELIVERY

    // For STORE_PICKUP
    private String pickupStoreName;
    private String pickupStoreAddress;
    private String pickupStoreCity;
    private String pickupStorePhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp DESC")
    @Builder.Default
    private List<TrackingStep> trackingSteps = new ArrayList<>();

    // Payment related fields
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String paymentStatus;  // PENDING, SUCCESS, FAILED

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to add items
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // Helper method to add tracking step
    public void addTrackingStep(TrackingStep step) {
        trackingSteps.add(step);
        step.setOrder(this);
    }
}