package com.college.shinecart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    private String deliveryType;  // HOME_DELIVERY or STORE_PICKUP

    // For HOME_DELIVERY
    private ShippingAddressDTO shippingAddress;

    // For STORE_PICKUP
    private Long storeId;

    // Order items from cart
    private List<OrderItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
        private String size;  // Optional for jewelry
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDTO {
        private String name;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String phone;
        private String email;
        private String landmark;
    }
}