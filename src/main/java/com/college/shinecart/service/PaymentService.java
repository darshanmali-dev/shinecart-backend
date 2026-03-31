package com.college.shinecart.service;

import com.college.shinecart.dto.RazorpayOrderResponse;
import com.college.shinecart.entity.Order;
import com.college.shinecart.entity.OrderItem;
import com.college.shinecart.entity.Product;
import com.college.shinecart.repository.OrderRepository;
import com.college.shinecart.repository.ProductRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final EmailService emailService;
    private final ProductRepository productRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Create Razorpay order for payment
     */
    public RazorpayOrderResponse createRazorpayOrder(String orderNumber) throws RazorpayException {
        // Get order from database
        Order order = orderService.getOrderByOrderNumber(orderNumber);

        // Create Razorpay order
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (order.getTotal() * 100)); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", orderNumber);

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        // Save razorpay order ID to database
        order.setRazorpayOrderId(razorpayOrder.get("id"));
        orderRepository.save(order);

        // Return response for frontend
        return RazorpayOrderResponse.builder()
                .razorpayOrderId(razorpayOrder.get("id"))
                .orderNumber(orderNumber)
                .amount(order.getTotal())
                .currency("INR")
                .key(keyId)  // Frontend needs this
                .build();
    }

    /**
     * Verify payment signature
     */
    @Transactional
    public boolean verifyPayment(String orderNumber, String razorpayOrderId,
                                 String razorpayPaymentId, String razorpaySignature) {
        try {
            // Create signature
            String generatedSignature = generateSignature(razorpayOrderId, razorpayPaymentId);

            // Verify signature
            if (generatedSignature.equals(razorpaySignature)) {
                // Payment successful - update order
                Order order = orderService.getOrderByOrderNumber(orderNumber);
                order.setRazorpayPaymentId(razorpayPaymentId);
                order.setRazorpaySignature(razorpaySignature);
                order.setPaymentStatus("SUCCESS");

                // Update order status to Processing
                orderService.updateOrderStatus(orderNumber, "Processing",
                        "Items are being prepared and quality checked");

                orderRepository.save(order);
                sendOrderConfirmationEmail(order, razorpayPaymentId, razorpayOrderId);

                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendOrderConfirmationEmail(Order order, String razorpayPaymentId, String razorpayOrderId) {
        try {
            // Get customer details from User entity
            String customerEmail = order.getUser().getEmail();
            String customerName = order.getUser().getUsername(); // ✅ Using username field

            // Build order items string
            String orderItems = "Order details"; // Default

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                orderItems = order.getItems().stream()
                        .map(item -> item.getProduct().getName() + " (Qty: " + item.getQuantity() + ")")
                        .collect(Collectors.joining(", "));
            }

            // Send the email
            emailService.sendOrderConfirmationEmail(
                    customerEmail,
                    customerName,
                    order.getOrderNumber(),
                    razorpayPaymentId,
                    razorpayOrderId,
                    order.getTotal(),
                    orderItems
            );

            System.out.println("✓ Order confirmation email sent to: " + customerEmail);

        } catch (Exception e) {
            // Log error but DON'T fail the payment
            System.err.println("✗ Failed to send email for order: " + order.getOrderNumber());
            e.printStackTrace();
            // Payment is still successful even if email fails
        }
    }
    /**
     * Generate signature for verification
     */
    private String generateSignature(String orderId, String paymentId) throws Exception {
        String payload = orderId + "|" + paymentId;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(keySecret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKey);

        byte[] hash = sha256_HMAC.doFinal(payload.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Handle payment failure
     */
    @Transactional
    public void handlePaymentFailure(String orderNumber) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        order.setPaymentStatus("FAILED");
        order.setStatus("Order Failed");

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() != null) {
                product.setStockQuantity(
                        product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        orderRepository.save(order);
    }
}