package com.college.shinecart.controller;

import com.college.shinecart.dto.AddToCartRequest;
import com.college.shinecart.dto.CartItemDTO;
import com.college.shinecart.dto.UpdateCartRequest;
import com.college.shinecart.entity.User;
import com.college.shinecart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class CartController {

    private final CartService cartService;

    // Get current user's cart
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart() {
        User user = getCurrentUser();
        return ResponseEntity.ok(cartService.getUserCart(user));
    }

    // Add product to cart
    @PostMapping
    public ResponseEntity<CartItemDTO> addToCart(@RequestBody AddToCartRequest request) {
        User user = getCurrentUser();
        CartItemDTO item = cartService.addToCart(user, request);
        return ResponseEntity.ok(item);
    }

    // Update cart item
    @PutMapping("/{productId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @PathVariable Long productId,
            @RequestBody UpdateCartRequest request
    ) {
        User user = getCurrentUser();
        CartItemDTO item = cartService.updateCartItem(user, productId, request);
        return ResponseEntity.ok(item);
    }

    // Remove product from cart
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, String>> removeFromCart(@PathVariable Long productId) {
        User user = getCurrentUser();
        cartService.removeFromCart(user, productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product removed from cart");
        return ResponseEntity.ok(response);
    }

    // Clear cart
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart() {
        User user = getCurrentUser();
        cartService.clearCart(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart cleared");
        return ResponseEntity.ok(response);
    }

    // Get cart total
    @GetMapping("/total")
    public ResponseEntity<Map<String, Double>> getCartTotal() {
        User user = getCurrentUser();
        Double total = cartService.getCartTotal(user);

        Map<String, Double> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }

    // Get cart item count
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartCount() {
        User user = getCurrentUser();
        Integer count = cartService.getCartItemCount(user);

        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // Helper method to get current authenticated user
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        throw new RuntimeException("User not authenticated");
    }
}