package com.college.shinecart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids", indexes = {
        @Index(name = "idx_auction_amount", columnList = "auction_id, amount DESC"),
        @Index(name = "idx_user_bidtime", columnList = "user_id, bid_time DESC")
})
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime bidTime;

    @Column(nullable = false)
    private Boolean isWinning = false;

    @Column(nullable = false)
    private Boolean autoExtend = false;

    // Constructors
    public Bid() {
        this.bidTime = LocalDateTime.now();
    }

    public Bid(Auction auction, User user, BigDecimal amount) {
        this();
        this.auction = auction;
        this.user = user;
        this.amount = amount;
    }

    public Bid(Auction auction, User user, BigDecimal amount, Boolean isWinning) {
        this(auction, user, amount);
        this.isWinning = isWinning;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Auction getAuction() {
        return auction;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public Boolean getIsWinning() {
        return isWinning;
    }

    public Boolean getAutoExtend() {
        return autoExtend;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public void setIsWinning(Boolean isWinning) {
        this.isWinning = isWinning;
    }

    public void setAutoExtend(Boolean autoExtend) {
        this.autoExtend = autoExtend;
    }

    // Business methods
    public boolean isValidBid(BigDecimal minimumBid) {
        return this.amount.compareTo(minimumBid) >= 0;
    }

    public void markAsWinning() {
        this.isWinning = true;
    }

    public void markAsNotWinning() {
        this.isWinning = false;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bid)) return false;
        Bid bid = (Bid) o;
        return id != null && id.equals(bid.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Bid{" +
                "id=" + id +
                ", auctionId=" + (auction != null ? auction.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", amount=" + amount +
                ", bidTime=" + bidTime +
                ", isWinning=" + isWinning +
                '}';
    }
}