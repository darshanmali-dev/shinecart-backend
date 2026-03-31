package com.college.shinecart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReportDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserReportItem {
        private String username;
        private String email;
        private String phone;
        private String createdAt;
        private String status;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderReportItem {
        private String orderNumber;
        private String customerName;
        private String orderDate;
        private String deliveryType;
        private String paymentStatus;
        private String status;
        private Double total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderReportResponse {
        private List<OrderReportItem> orders;
        private Integer totalOrders;
        private Double totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductReportItem {
        private String productName;
        private String category;
        private String metal;
        private Integer totalQuantitySold;
        private Double totalRevenue;
    }
}