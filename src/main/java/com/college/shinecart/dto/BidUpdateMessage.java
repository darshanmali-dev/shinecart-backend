package com.college.shinecart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidUpdateMessage {

    private Long auctionId;
    private BigDecimal newPrice;
    private String bidderName;
    private LocalDateTime bidTime;
    private Integer totalBids;
    private Long timeRemaining; // seconds
    private Boolean timeExtended;

    // Constructors
    public BidUpdateMessage() {}

    public BidUpdateMessage(Long auctionId, BigDecimal newPrice, String bidderName,
                            LocalDateTime bidTime, Integer totalBids, Long timeRemaining) {
        this.auctionId = auctionId;
        this.newPrice = newPrice;
        this.bidderName = bidderName;
        this.bidTime = bidTime;
        this.totalBids = totalBids;
        this.timeRemaining = timeRemaining;
        this.timeExtended = false;
    }

    // Getters
    public Long getAuctionId() {
        return auctionId;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public String getBidderName() {
        return bidderName;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public Integer getTotalBids() {
        return totalBids;
    }

    public Long getTimeRemaining() {
        return timeRemaining;
    }

    public Boolean getTimeExtended() {
        return timeExtended;
    }

    // Setters
    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public void setTotalBids(Integer totalBids) {
        this.totalBids = totalBids;
    }

    public void setTimeRemaining(Long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public void setTimeExtended(Boolean timeExtended) {
        this.timeExtended = timeExtended;
    }
}