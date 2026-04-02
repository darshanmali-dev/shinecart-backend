package com.college.shinecart.controller;

import com.college.shinecart.entity.Order;
import com.college.shinecart.entity.Product;
import com.college.shinecart.repository.OrderRepository;
import com.college.shinecart.repository.UserRepository;
import com.college.shinecart.repository.ProductRepository;
import com.college.shinecart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class AdminStatsController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalOrders  = orderRepository.count();
        // User stats
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("ROLE_ADMIN"))
                .count();
        long regularUsers = totalUsers - adminUsers;

        // Product stats
        long totalProducts = productRepository.count();
        long inStockProducts = productRepository.findAll().stream()
                .filter(p -> "In Stock".equals(p.getAvailability()))
                .count();
        long lowStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() < 5)
                .count();

        // Cart stats (as proxy for potential orders)
        long totalCartItems = cartRepository.count();


        double estimatedRevenue = orderRepository.getTotalRevenue(); // Mock: 10% conversion

        stats.put("totalOrders", totalOrders);
        stats.put("activeCustomers", regularUsers);
        stats.put("totalProducts", totalProducts);
        stats.put("inStockProducts", inStockProducts);
        stats.put("lowStockProducts", lowStockProducts);
        stats.put("revenue", Math.round(estimatedRevenue));
        stats.put("totalCartItems", totalCartItems);

        return ResponseEntity.ok(stats);
    }
}