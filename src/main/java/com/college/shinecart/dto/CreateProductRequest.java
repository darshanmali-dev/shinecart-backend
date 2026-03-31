package com.college.shinecart.dto;

import lombok.Data;

@Data
public class CreateProductRequest {
    private String name;
    private Double price;
    private Double originalPrice;
    private String category;
    private String metal;
    private String stone;
    private Integer discount;
    private String badge;
    private String sku;
    private String availability;
    private String sizes;  // Comma-separated
    private String description;
    private String features;  // JSON string
    private String specifications;  // JSON string
    private Integer stockQuantity;
}