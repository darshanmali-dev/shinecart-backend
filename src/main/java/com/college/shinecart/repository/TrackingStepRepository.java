package com.college.shinecart.repository;

import com.college.shinecart.entity.Order;
import com.college.shinecart.entity.TrackingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingStepRepository extends JpaRepository<TrackingStep, Long> {

    // Find all tracking steps for an order
    List<TrackingStep> findByOrderOrderByTimestampDesc(Order order);

    // Find current tracking step
    TrackingStep findByOrderAndCurrentTrue(Order order);
}