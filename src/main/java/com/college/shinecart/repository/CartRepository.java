package com.college.shinecart.repository;

import com.college.shinecart.entity.Cart;
import com.college.shinecart.entity.User;
import com.college.shinecart.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Get all cart items for a user
    List<Cart> findByUser(User user);

    // Find specific cart item
    Optional<Cart> findByUserAndProduct(User user, Product product);

    // Check if product is in user's cart
    boolean existsByUserAndProduct(User user, Product product);

    // Delete cart item
    void deleteByUserAndProduct(User user, Product product);

    // Delete all cart items for user
    void deleteByUser(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}