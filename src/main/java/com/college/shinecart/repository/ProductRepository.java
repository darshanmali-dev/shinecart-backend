package com.college.shinecart.repository;

import com.college.shinecart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by category
    List<Product> findByCategory(String category);

    // Find by metal
    List<Product> findByMetal(String metal);

    // Find by category and metal
    List<Product> findByCategoryAndMetal(String category, String metal);

    // Search by name
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Find by badge
    List<Product> findByBadge(String badge);

    // Find by SKU
    Product findBySku(String sku);

    // Custom query for filtering with multiple criteria
    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:metal IS NULL OR p.metal = :metal) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> findByFilters(
            @Param("category") String category,
            @Param("metal") String metal,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}