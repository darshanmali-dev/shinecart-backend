package com.college.shinecart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {

    private String razorpayOrderId;
    private String orderNumber;
    private Double amount;  // In rupees
    private String currency;
    private String key;  // Razorpay Key ID (for frontend)
}