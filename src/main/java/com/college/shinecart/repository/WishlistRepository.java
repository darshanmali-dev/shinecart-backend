package com.college.shinecart.repository;

import com.college.shinecart.entity.Wishlist;
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
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Get all wishlist items for a user
    List<Wishlist> findByUser(User user);

    // Find specific wishlist item
    Optional<Wishlist> findByUserAndProduct(User user, Product product);

    // Check if product is in user's wishlist
    boolean existsByUserAndProduct(User user, Product product);

    // Delete wishlist item
    void deleteByUserAndProduct(User user, Product product);

    @Transactional
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}