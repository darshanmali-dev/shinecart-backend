package com.college.shinecart.service;

import com.college.shinecart.dto.AddToWishlistRequest;
import com.college.shinecart.dto.WishlistItemDTO;
import com.college.shinecart.entity.Product;
import com.college.shinecart.entity.User;
import com.college.shinecart.entity.Wishlist;
import com.college.shinecart.repository.ProductRepository;
import com.college.shinecart.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    // Get user's wishlist
    public List<WishlistItemDTO> getUserWishlist(User user) {
        List<Wishlist> wishlistItems = wishlistRepository.findByUser(user);
        return wishlistItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Add product to wishlist
    public WishlistItemDTO addToWishlist(User user, AddToWishlistRequest request) {
        // Check if product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Product already in wishlist");
        }

        // Create wishlist item
        Wishlist wishlistItem = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistItem = wishlistRepository.save(wishlistItem);
        return convertToDTO(wishlistItem);
    }

    // Remove product from wishlist
    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Product not in wishlist");
        }

        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    // Check if product is in wishlist
    public boolean isInWishlist(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return wishlistRepository.existsByUserAndProduct(user, product);
    }

    // Clear entire wishlist
    @Transactional
    public void clearWishlist(User user) {
        List<Wishlist> items = wishlistRepository.findByUser(user);
        wishlistRepository.deleteAll(items);
    }

    // Convert Wishlist to WishlistItemDTO
    private WishlistItemDTO convertToDTO(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        return WishlistItemDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .rating(product.getRating())
                .reviews(product.getReviews())
                .image(product.getImage())
                .category(product.getCategory())
                .availability(product.getAvailability())
                .build();
    }
}