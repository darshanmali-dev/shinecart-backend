package com.college.shinecart.dto;

public class AuctionStatsDTO {

    private Long totalAuctions;
    private Long activeAuctions;
    private Long totalBids;
    private Double totalRevenue;

    // Constructors
    public AuctionStatsDTO() {}

    // Getters
    public Long getTotalAuctions() {
        return totalAuctions;
    }

    public Long getActiveAuctions() {
        return activeAuctions;
    }

    public Long getTotalBids() {
        return totalBids;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    // Setters
    public void setTotalAuctions(Long totalAuctions) {
        this.totalAuctions = totalAuctions;
    }

    public void setActiveAuctions(Long activeAuctions) {
        this.activeAuctions = activeAuctions;
    }

    public void setTotalBids(Long totalBids) {
        this.totalBids = totalBids;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}