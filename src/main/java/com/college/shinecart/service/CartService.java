package com.college.shinecart.service;

import com.college.shinecart.dto.AddToCartRequest;
import com.college.shinecart.dto.CartItemDTO;
import com.college.shinecart.dto.UpdateCartRequest;
import com.college.shinecart.entity.Cart;
import com.college.shinecart.entity.Product;
import com.college.shinecart.entity.User;
import com.college.shinecart.repository.CartRepository;
import com.college.shinecart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    // Get user's cart
    public List<CartItemDTO> getUserCart(User user) {
        List<Cart> cartItems = cartRepository.findByUser(user);
        return cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Add product to cart
    public CartItemDTO addToCart(User user, AddToCartRequest request) {
        // Check if product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if already in cart
        Cart existingCartItem = cartRepository.findByUserAndProduct(user, product).orElse(null);

        if (existingCartItem != null) {
            // Check stock before increasing quantity
            int newQuantity = existingCartItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() != null && newQuantity > product.getStockQuantity()) {
                throw new RuntimeException(
                        "Only " + product.getStockQuantity() + " items available in stock");
            }
            // Update quantity
            existingCartItem.setQuantity(newQuantity);
            existingCartItem.setSize(request.getSize());
            existingCartItem = cartRepository.save(existingCartItem);
            return convertToDTO(existingCartItem);
        }


        if (product.getStockQuantity() != null &&
                request.getQuantity() > product.getStockQuantity()) {
            throw new RuntimeException(
                    "Only " + product.getStockQuantity() + " items available in stock");
        }
        // Create new cart item
        Cart cartItem = Cart.builder()
                .user(user)
                .product(product)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .size(request.getSize())
                .build();

        cartItem = cartRepository.save(cartItem);
        return convertToDTO(cartItem);
    }

    // Update cart item
    public CartItemDTO updateCartItem(User user, Long productId, UpdateCartRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cartItem = cartRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new RuntimeException("Product not in cart"));

        if (request.getQuantity() != null) {
            if (product.getStockQuantity() != null &&
                    request.getQuantity() > product.getStockQuantity()) {
                throw new RuntimeException(
                        "Only " + product.getStockQuantity() + " items available in stock");
            }
            cartItem.setQuantity(request.getQuantity());
        }
        if (request.getSize() != null) {
            cartItem.setSize(request.getSize());
        }

        cartItem = cartRepository.save(cartItem);
        return convertToDTO(cartItem);
    }

    // Remove product from cart
    @Transactional
    public void removeFromCart(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!cartRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Product not in cart");
        }

        cartRepository.deleteByUserAndProduct(user, product);
    }

    // Clear entire cart
    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }

    // Get cart total
    public Double getCartTotal(User user) {
        List<Cart> cartItems = cartRepository.findByUser(user);
        return cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    // Get cart item count
    public Integer getCartItemCount(User user) {
        List<Cart> cartItems = cartRepository.findByUser(user);
        return cartItems.stream()
                .mapToInt(Cart::getQuantity)
                .sum();
    }

    // Convert Cart to CartItemDTO
    private CartItemDTO convertToDTO(Cart cart) {
        Product product = cart.getProduct();
        return CartItemDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .quantity(cart.getQuantity())
                .size(cart.getSize())
                .image(product.getImage())
                .category(product.getCategory())
                .availability(product.getAvailability())
                .build();
    }
}