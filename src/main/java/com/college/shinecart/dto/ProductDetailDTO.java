package com.college.shinecart.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDTO {
    private Long id;
    private String name;
    private Double price;
    private Double originalPrice;
    private Double rating;
    private Integer reviews;
    private String category;
    private String metal;
    private String stone;
    private Integer discount;
    private String badge;
    private String sku;
    private String availability;
    private List<String> images;
    private List<String> sizes;
    private Integer stockQuantity;
    private String description;
    private List<String> features;
    private List<Specification> specifications;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Specification {
        private String label;
        private String value;
    }
}