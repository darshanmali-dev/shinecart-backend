package com.college.shinecart.dto;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Integer quantity;
    private String size;
}