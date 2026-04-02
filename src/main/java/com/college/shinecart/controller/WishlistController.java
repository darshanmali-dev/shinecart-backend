package com.college.shinecart.controller;

import com.college.shinecart.dto.AddToWishlistRequest;
import com.college.shinecart.dto.WishlistItemDTO;
import com.college.shinecart.entity.User;
import com.college.shinecart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class WishlistController {

    private final WishlistService wishlistService;

    // Get current user's wishlist
    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> getWishlist() {
        User user = getCurrentUser();
        return ResponseEntity.ok(wishlistService.getUserWishlist(user));
    }

    // Add product to wishlist
    @PostMapping
    public ResponseEntity<WishlistItemDTO> addToWishlist(@RequestBody AddToWishlistRequest request) {
        User user = getCurrentUser();
        WishlistItemDTO item = wishlistService.addToWishlist(user, request);
        return ResponseEntity.ok(item);
    }

    // Remove product from wishlist
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, String>> removeFromWishlist(@PathVariable Long productId) {
        User user = getCurrentUser();
        wishlistService.removeFromWishlist(user, productId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product removed from wishlist");
        return ResponseEntity.ok(response);
    }

    // Check if product is in wishlist
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(@PathVariable Long productId) {
        User user = getCurrentUser();
        boolean isInWishlist = wishlistService.isInWishlist(user, productId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isInWishlist", isInWishlist);
        return ResponseEntity.ok(response);
    }

    // Clear wishlist
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearWishlist() {
        User user = getCurrentUser();
        wishlistService.clearWishlist(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Wishlist cleared");
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