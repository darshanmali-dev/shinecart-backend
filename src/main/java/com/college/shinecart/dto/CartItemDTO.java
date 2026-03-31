package com.college.shinecart.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long id;
    private String name;
    private Double price;
    private Double originalPrice;
    private Integer quantity;
    private String size;
    private String image;
    private String category;
    private String availability;
}