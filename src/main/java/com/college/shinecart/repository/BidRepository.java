package com.college.shinecart.repository;

import com.college.shinecart.entity.Bid;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    // Find all bids for an auction (newest first)
    List<Bid> findByAuctionIdOrderByBidTimeDesc(Long auctionId);

    // Find all bids for an auction (highest first)
    List<Bid> findByAuctionIdOrderByAmountDesc(Long auctionId);

    // Find all bids by a user (newest first)
    List<Bid> findByUserIdOrderByBidTimeDesc(Long userId);

    // Find highest bid for an auction
    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);

    // Find current winning bid
    Optional<Bid> findByAuctionIdAndIsWinningTrue(Long auctionId);

    // Count total bids for an auction
    Integer countByAuctionId(Long auctionId);

    // Count bids by a user for an auction
    Integer countByAuctionIdAndUserId(Long auctionId, Long userId);

    // Check if user has bid on auction
    boolean existsByAuctionIdAndUserId(Long auctionId, Long userId);

    // Get user's highest bid for an auction
    @Query("SELECT MAX(b.amount) FROM Bid b WHERE b.auction.id = :auctionId AND b.user.id = :userId")
    Optional<BigDecimal> findMaxBidByUserForAuction(
            @Param("auctionId") Long auctionId,
            @Param("userId") Long userId
    );

    // Find all winning bids by user
    List<Bid> findByUserIdAndIsWinningTrueOrderByBidTimeDesc(Long userId);

    // Get total bids count for statistics
    @Query("SELECT COUNT(b) FROM Bid b")
    Long getTotalBidsCount();

    @Transactional
    @Modifying
    @Query("DELETE FROM Bid b WHERE b.auction.id = :auctionId")
    void deleteByAuctionId(@Param("auctionId") Long auctionId);
}