package com.college.shinecart.controller;

import com.college.shinecart.dto.CreateOrderRequest;
import com.college.shinecart.dto.OrderResponse;
import com.college.shinecart.entity.Order;
import com.college.shinecart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController

@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class OrderController {

    private final OrderService orderService;


    /**
     * Get all orders (admin only)
     * GET /api/orders/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> response = orders.stream()
                .map(orderService::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get order stats (admin only)
     * GET /api/orders/admin/stats
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        return ResponseEntity.ok(orderService.getOrderStats());
    }
    /**
     * Create a new order (Proceed to Checkout)
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        try {
            // Get user ID from authentication
            Long userId = getUserIdFromAuth(authentication);
            System.out.println("Creating order for user ID: " + userId);
            System.out.println("Order request: " + request);

            // Create order
            Order order = orderService.createOrder(request, userId);

            // Convert to response
            OrderResponse response = orderService.toOrderResponse(order);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get order by order number (for tracking page)
     * GET /api/orders/{orderNumber}
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        OrderResponse response = orderService.toOrderResponse(order);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders for logged-in user
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);

        List<Order> orders = orderService.getUserOrders(userId);

        List<OrderResponse> response = orders.stream()
                .map(orderService::toOrderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (for admin - will add later)
     * PUT /api/orders/{orderNumber}/status
     */
    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable String orderNumber,
            @RequestParam String status,
            @RequestParam String description) {

        orderService.updateOrderStatus(orderNumber, status, description);
        return ResponseEntity.ok("Order status updated successfully");
    }

    /**
     * Helper method to get user ID from authentication
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof com.college.shinecart.entity.User) {
            com.college.shinecart.entity.User user = (com.college.shinecart.entity.User) principal;
            // Just return the ID directly - don't print the user object!
            return user.getId();
        } else {
            throw new RuntimeException("Cannot cast principal to User");
        }
    }
}