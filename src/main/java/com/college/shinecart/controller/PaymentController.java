package com.college.shinecart.controller;

import com.college.shinecart.dto.PaymentRequest;
import com.college.shinecart.dto.RazorpayOrderResponse;
import com.college.shinecart.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create Razorpay order
     * POST /api/payments/create-order
     */
    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponse> createOrder(@RequestParam String orderNumber) {
        try {
            RazorpayOrderResponse response = paymentService.createRazorpayOrder(orderNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    /**
     * Verify payment
     * POST /api/payments/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody PaymentRequest request) {
        boolean isValid = paymentService.verifyPayment(
                request.getOrderNumber(),
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        Map<String, Object> response = new HashMap<>();
        if (isValid) {
            response.put("success", true);
            response.put("message", "Payment verified successfully");
            return ResponseEntity.ok(response);
        } else {
            paymentService.handlePaymentFailure(request.getOrderNumber());
            response.put("success", false);
            response.put("message", "Payment verification failed");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Handle payment failure
     * POST /api/payments/failure
     */
    @PostMapping("/failure")
    public ResponseEntity<Map<String, String>> handleFailure(@RequestParam String orderNumber) {
        paymentService.handlePaymentFailure(orderNumber);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment failed");
        return ResponseEntity.ok(response);
    }
}