package com.college.shinecart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;  // Product name snapshot

    @Column(nullable = false)
    private Double price;  // Price at time of order

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String image;  // Product image snapshot

    // For jewelry - size if applicable
    private String size;
}