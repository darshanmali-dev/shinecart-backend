package com.college.shinecart.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal startingPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal reservePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bidIncrement;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuctionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(nullable = false)
    private Integer totalBids = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Auction() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Auction(Product product, BigDecimal startingPrice, BigDecimal reservePrice,
                   BigDecimal bidIncrement, LocalDateTime startTime, LocalDateTime endTime,
                   User createdBy) {
        this();
        this.product = product;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.currentPrice = startingPrice;
        this.bidIncrement = bidIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdBy = createdBy;
        this.status = startTime.isAfter(LocalDateTime.now()) ?
                AuctionStatus.UPCOMING : AuctionStatus.ACTIVE;
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getBidIncrement() {
        return bidIncrement;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public User getWinner() {
        return winner;
    }

    public Integer getTotalBids() {
        return totalBids;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setBidIncrement(BigDecimal bidIncrement) {
        this.bidIncrement = bidIncrement;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public void setTotalBids(Integer totalBids) {
        this.totalBids = totalBids;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods
    public boolean isActive() {
        return this.status == AuctionStatus.ACTIVE ||
                this.status == AuctionStatus.ENDING_SOON;
    }

    public boolean hasEnded() {
        return this.status == AuctionStatus.ENDED ||
                this.status == AuctionStatus.CANCELLED;
    }

    public boolean canPlaceBid() {
        return isActive() && LocalDateTime.now().isBefore(this.endTime);
    }

    public void incrementBidCount() {
        this.totalBids++;
    }

    public BigDecimal getMinimumBid() {
        return this.currentPrice.add(this.bidIncrement);
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auction)) return false;
        Auction auction = (Auction) o;
        return id != null && id.equals(auction.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Auction{" +
                "id=" + id +
                ", productId=" + (product != null ? product.getId() : null) +
                ", currentPrice=" + currentPrice +
                ", status=" + status +
                ", endTime=" + endTime +
                '}';
    }
}