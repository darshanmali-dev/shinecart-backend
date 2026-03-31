package com.college.shinecart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private String orderNumber;  // Order to be paid for
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}