package com.college.shinecart.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistItemDTO {
    private Long id;
    private String name;
    private Double price;
    private Double originalPrice;
    private Double rating;
    private Integer reviews;
    private String image;
    private String category;
    private String availability;
}