package com.college.shinecart.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateAuctionRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Starting price is required")
    @DecimalMin(value = "0.01", message = "Starting price must be greater than 0")
    private BigDecimal startingPrice;

    @DecimalMin(value = "0.01", message = "Reserve price must be greater than 0")
    private BigDecimal reservePrice;

    @NotNull(message = "Bid increment is required")
    @DecimalMin(value = "1.00", message = "Bid increment must be at least ₹1")
    private BigDecimal bidIncrement;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    // Constructors
    public CreateAuctionRequest() {}

    public CreateAuctionRequest(Long productId, BigDecimal startingPrice,
                                BigDecimal reservePrice, BigDecimal bidIncrement,
                                LocalDateTime startTime, LocalDateTime endTime) {
        this.productId = productId;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.bidIncrement = bidIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public Long getProductId() {
        return productId;
    }

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
    public void setProductId(Long productId) {
        this.productId = productId;
    }

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

    // Validation method
    public boolean isValid() {
        if (endTime != null && startTime != null) {
            return endTime.isAfter(startTime);
        }
        return true;
    }

    @Override
    public String toString() {
        return "CreateAuctionRequest{" +
                "productId=" + productId +
                ", startingPrice=" + startingPrice +
                ", reservePrice=" + reservePrice +
                ", bidIncrement=" + bidIncrement +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}