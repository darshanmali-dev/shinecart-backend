package com.college.shinecart.entity;

public enum AuctionStatus {
    UPCOMING("Auction has not started yet"),
    ACTIVE("Auction is currently accepting bids"),
    ENDING_SOON("Auction is ending in less than 5 minutes"),
    ENDED("Auction has ended"),
    CANCELLED("Auction was cancelled by admin");

    private final String description;

    AuctionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOpenForBidding() {
        return this == ACTIVE || this == ENDING_SOON;
    }

    public boolean isFinished() {
        return this == ENDED || this == CANCELLED;
    }
}
