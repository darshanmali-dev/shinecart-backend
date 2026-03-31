package com.college.shinecart.repository;

import com.college.shinecart.entity.Auction;
import com.college.shinecart.entity.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    // Find by status
    List<Auction> findByStatus(AuctionStatus status);

    // Find by status ordered by end time
    List<Auction> findByStatusOrderByEndTimeAsc(AuctionStatus status);

    // Find auctions that should have started
    @Query("SELECT a FROM Auction a WHERE a.startTime <= :now AND a.status = 'UPCOMING'")
    List<Auction> findAuctionsToStart(@Param("now") LocalDateTime now);

    // Find expired auctions
    @Query("SELECT a FROM Auction a WHERE a.endTime < :now AND a.status IN ('ACTIVE', 'ENDING_SOON')")
    List<Auction> findExpiredAuctions(@Param("now") LocalDateTime now);

    // Find auctions ending soon (within minutes)
    @Query("SELECT a FROM Auction a WHERE a.endTime BETWEEN :now AND :endTime AND a.status = 'ACTIVE'")
    List<Auction> findAuctionsEndingSoon(
            @Param("now") LocalDateTime now,
            @Param("endTime") LocalDateTime endTime
    );

    // Find by product
    List<Auction> findByProductId(Long productId);

    // Find by creator
    List<Auction> findByCreatedById(Long userId);

    // Find by winner
    List<Auction> findByWinnerId(Long userId);

    // Get active auction count
    @Query("SELECT COUNT(a) FROM Auction a WHERE a.status IN ('ACTIVE', 'ENDING_SOON')")
    Long countActiveAuctions();

    // Get total revenue from completed auctions
    @Query("SELECT COALESCE(SUM(a.currentPrice), 0) FROM Auction a WHERE a.status = 'ENDED' AND a.winner IS NOT NULL")
    Double getTotalRevenue();

    boolean existsByProductId(Long productId);
}