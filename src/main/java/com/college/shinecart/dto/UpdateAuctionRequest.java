package com.college.shinecart.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateAuctionRequest {

    @DecimalMin(value = "0.01", message = "Starting price must be greater than 0")
    private BigDecimal startingPrice;

    @DecimalMin(value = "0.01", message = "Reserve price must be greater than 0")
    private BigDecimal reservePrice;

    @DecimalMin(value = "1.00", message = "Bid increment must be at least ₹1")
    private BigDecimal bidIncrement;

    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    // Constructors
    public UpdateAuctionRequest() {}

    public UpdateAuctionRequest(BigDecimal startingPrice, BigDecimal reservePrice,
                                BigDecimal bidIncrement, LocalDateTime startTime,
                                LocalDateTime endTime) {
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.bidIncrement = bidIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
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

    // Setters
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
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

    // Validation
    public boolean isValid() {
        if (endTime != null && startTime != null) {
            return endTime.isAfter(startTime);
        }
        return true;
    }

    @Override
    public String toString() {
        return "UpdateAuctionRequest{" +
                "startingPrice=" + startingPrice +
                ", reservePrice=" + reservePrice +
                ", bidIncrement=" + bidIncrement +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}