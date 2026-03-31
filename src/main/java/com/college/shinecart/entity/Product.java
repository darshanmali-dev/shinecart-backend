package com.college.shinecart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double originalPrice;

    private Double rating = 0.0;

    private Integer reviews = 0;

    @Column(length = 500)
    private String image;  // Primary image URL

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String metal;

    private String stone;

    private Integer discount = 0;

    private String badge;

    @Column(nullable = false)
    private String sku;

    private String availability = "In Stock";

    @Column(length = 1000)
    private String images;  // Comma-separated URLs: "url1,url2,url3"

    @Column(length = 500)
    private String sizes;  // Comma-separated: "5,6,7,8,9"

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String features;  // JSON array as string: ["feature1","feature2"]

    @Column(length = 2000)
    private String specifications;  // JSON array of objects

    private Integer stockQuantity = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Auction> auctions = new ArrayList<>();

    // Getter and setter
    public List<Auction> getAuctions() {
        return auctions;
    }

    public void setAuctions(List<Auction> auctions) {
        this.auctions = auctions;
    }
}