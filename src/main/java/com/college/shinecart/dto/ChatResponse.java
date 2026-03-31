package com.college.shinecart.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String message;  // Bot's text response
    private String intent;   // Detected intent
    private String sessionId;  // Conversation tracking

    // Reuse existing DTOs
    private List<ProductDTO> products;  // For product search results
    private List<CartItemDTO> cartItems;  // For "show my cart"
    private OrderResponse order;  // For "track order"

    private List<QuickAction> quickActions;  // Suggested buttons
    private ContactInfo contactInfo;  // Contact details for fallback

    // Quick action buttons
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickAction {
        private String label;  // "View Cart", "Track Order", etc.
        private String action;  // "view_cart", "track_order", etc.
        private String icon;   // Icon name for frontend (lucide-react)
        private String data;   // Optional: extra data (e.g., product ID)
    }

    // Contact information
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactInfo {
        private String phone;
        private String email;
        private String workingHours;
        private String address;
    }
}