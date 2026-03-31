package com.college.shinecart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long userId;
    private String userName;
    private String userEmail;
    private String orderNumber;
    private String status;
    private String orderDate;  // Format: "2024-01-20"
    private String expectedDelivery;  // Format: "2024-01-25"
    private Double total;
    private String deliveryType;  // HOME_DELIVERY or STORE_PICKUP

    private List<OrderItemResponse> items;
    private ShippingAddressResponse shippingAddress;  // Only for HOME_DELIVERY
    private StorePickupResponse storePickup;  // Only for STORE_PICKUP
    private List<TrackingStepResponse> trackingSteps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private String name;
        private Double price;
        private Integer quantity;
        private String image;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressResponse {
        private String name;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorePickupResponse {
        private String storeName;
        private String storeAddress;
        private String storeCity;
        private String storePhone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingStepResponse {
        private String status;
        private String date;
        private String time;
        private String description;
        private Boolean completed;
        private Boolean current;
        private String icon;
    }
}