package com.college.shinecart.dto;

import com.college.shinecart.entity.AuctionStatus;
import java.math.BigDecimal;

public class AuctionStatusMessage {

    private Long auctionId;
    private AuctionStatus status;
    private String winnerName;
    private Long winnerId;
    private BigDecimal finalPrice;
    private String message;

    // Constructors
    public AuctionStatusMessage() {}

    public AuctionStatusMessage(Long auctionId, AuctionStatus status) {
        this.auctionId = auctionId;
        this.status = status;
    }

    // Getters
    public Long getAuctionId() {
        return auctionId;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}