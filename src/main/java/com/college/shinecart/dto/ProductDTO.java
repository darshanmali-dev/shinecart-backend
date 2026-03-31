package com.college.shinecart.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private Double originalPrice;
    private Double rating;
    private Integer reviews;
    private String image;
    private String category;
    private String metal;
    private Integer discount;
    private String badge;
    private String availability;
    private Integer stockQuantity;
    private String sku;
    private String stone;
}