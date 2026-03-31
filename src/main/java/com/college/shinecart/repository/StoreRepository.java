package com.college.shinecart.repository;

import com.college.shinecart.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // Find all active stores
    List<Store> findByActiveTrue();

    // Find stores by city
    List<Store> findByCityAndActiveTrue(String city);

    // Find stores by state
    List<Store> findByStateAndActiveTrue(String state);
}