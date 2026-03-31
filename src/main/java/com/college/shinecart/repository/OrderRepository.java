package com.college.shinecart.repository;

import com.college.shinecart.entity.Order;
import com.college.shinecart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find all orders for a user
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // Find orders by user ID
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find orders by status
    List<Order> findByStatus(String status);

    List<Order> findAllByOrderByCreatedAtDesc();

    // Find orders by user and status
    List<Order> findByUserAndStatus(User user, String status);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.product.id = :productId")
    boolean existsByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o")
    Double getTotalRevenue();
    // or if orders have order items:

    List<Order> findByOrderDateBetween(
            LocalDate from, LocalDate to);

}