package com.college.shinecart.service;

import com.college.shinecart.dto.*;
import com.college.shinecart.entity.Order;
import com.college.shinecart.entity.User;
import com.college.shinecart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final AuctionService auctionService;

    /**
     * Process user message and generate bot response
     */
    public ChatResponse processMessage(ChatRequest request, User user) {
        String message = request.getMessage().toLowerCase().trim();
        String sessionId = request.getSessionId();

        // Detect intent and handle
        if (isOrderQuery(message)) {
            return handleOrderQuery(message, user, sessionId);
        } else if (isProductQuery(message)) {
            return handleProductQuery(message, user, sessionId);
        } else if (isCartQuery(message)) {
            return handleCartQuery(user, sessionId);
        } else if (isAuctionQuery(message)) {
            return handleAuctionQuery(user, sessionId);
        } else if (isPaymentQuery(message)) {         // ← add this
            return handlePaymentQuery(sessionId);
        }else if (isHelpQuery(message)) {
            return handleHelpQuery(sessionId);
        } else {
            return handleContactInfo(sessionId);
        }
    }

    // ==================== Intent Detection ====================

    private boolean isOrderQuery(String message) {
        return containsAny(message, "order", "track","track_order", "delivery", "shipped", "status");
    }

    private boolean isPaymentQuery(String message) {
        return containsAny(message, "payment", "pay", "razorpay", "upi", "transaction");
    }



    private boolean isProductQuery(String message) {
        return containsAny(message,
                "find", "search", "show", "looking for", "want",
                "ring", "necklace", "bracelet", "earring",
                "gold", "silver", "platinum", "diamond"
        );
    }

    private boolean isCartQuery(String message) {
        return containsAny(message, "cart", "basket", "shopping");
    }

    private boolean isAuctionQuery(String message) {
        return containsAny(message, "auction", "bid", "bidding", "live");
    }

    private boolean isHelpQuery(String message) {
        return containsAny(message, "help", "support", "how", "what can you do");
    }

    private boolean containsAny(String message, String... keywords) {
        return Arrays.stream(keywords).anyMatch(message::contains);
    }

    // ==================== Intent Handlers ====================

    /**
     * Handle order tracking queries
     */

    private ChatResponse handlePaymentQuery(String sessionId) {
        return ChatResponse.builder()
                .message(
                        "💳 We support the following payment methods:\n\n" +
                                "• UPI (Google Pay, PhonePe, Paytm)\n" +
                                "• Credit / Debit Cards\n" +
                                "• Net Banking\n" +
                                "• Wallets\n\n" +
                                "All payments are secured by Razorpay 🔒"
                )
                .intent("payment_info")
                .sessionId(sessionId)
                .quickActions(List.of(
                        ChatResponse.QuickAction.builder()
                                .label("Go to Checkout")
                                .action("checkout")
                                .icon("CreditCard")
                                .build()
                ))
                .build();
    }
    private ChatResponse handleOrderQuery(String message, User user, String sessionId) {
        if (user == null) {
            return ChatResponse.builder()
                    .message("Please login to track your orders.")
                    .intent("order_login_required")
                    .sessionId(sessionId)
                    .quickActions(List.of(
                            ChatResponse.QuickAction.builder()
                                    .label("Login")
                                    .action("login")
                                    .icon("LogIn")
                                    .build()
                    ))
                    .build();
        }

        // Extract order number from message if present
        String orderNumber = extractOrderNumber(message);

        if (orderNumber != null) {
            try {
                // Get Order entity
                Order order = orderService.getOrderByOrderNumber(orderNumber);

                // Convert to OrderResponse DTO
                OrderResponse orderResponse = orderService.toOrderResponse(order);

                return ChatResponse.builder()
                        .message(String.format(
                                "📦 Order #%s\n" +
                                        "Status: %s\n" +
                                        "Expected Delivery: %s\n" +
                                        "Total: ₹%,.2f",
                                orderResponse.getOrderNumber(),
                                orderResponse.getStatus(),
                                orderResponse.getExpectedDelivery(),
                                orderResponse.getTotal()
                        ))
                        .intent("order_found")
                        .sessionId(sessionId)
                        .order(orderResponse)
                        .quickActions(List.of(
                                ChatResponse.QuickAction.builder()
                                        .label("View Full Details")
                                        .action("view_order")
                                        .icon("Package")
                                        .data(orderNumber)
                                        .build()
                        ))
                        .build();
            } catch (Exception e) {
                return ChatResponse.builder()
                        .message("I couldn't find order #" + orderNumber + ". Please check the order number and try again.")
                        .intent("order_not_found")
                        .sessionId(sessionId)
                        .build();
            }
        } else {
            // Ask for order number
            return ChatResponse.builder()
                    .message("Please provide your order number to track your order.\n\nExample: track order SC240120001")
                    .intent("order_number_required")
                    .sessionId(sessionId)
                    .build();
        }
    }

    /**
     * Handle product search queries
     */
    private ChatResponse handleProductQuery(String message, User user, String sessionId) {
        // Extract search terms
        String searchTerm = extractProductSearchTerm(message);

        // Search products
        List<ProductDTO> products = productService.searchProducts(searchTerm);

        if (products.isEmpty()) {
            return ChatResponse.builder()
                    .message("I couldn't find any products matching '" + searchTerm + "'. Try searching for gold rings, silver necklaces, or diamond earrings.")
                    .intent("product_not_found")
                    .sessionId(sessionId)
                    .quickActions(getProductQuickActions())
                    .build();
        }

        // Limit to top 5 results
        List<ProductDTO> topProducts = products.stream()
                .limit(5)
                .collect(Collectors.toList());

        return ChatResponse.builder()
                .message(String.format(
                        "I found %d products matching '%s'. Here are the top matches:",
                        products.size(),
                        searchTerm
                ))
                .intent("product_found")
                .sessionId(sessionId)
                .products(topProducts)
                .quickActions(List.of(
                        ChatResponse.QuickAction.builder()
                                .label("View All " + products.size() + " Products")
                                .action("view_all_products")
                                .icon("Grid")
                                .data(searchTerm)
                                .build()
                ))
                .build();
    }

    /**
     * Handle cart queries
     */
    private ChatResponse handleCartQuery(User user, String sessionId) {
        if (user == null) {
            return ChatResponse.builder()
                    .message("Please login to view your cart.")
                    .intent("cart_login_required")
                    .sessionId(sessionId)
                    .quickActions(List.of(
                            ChatResponse.QuickAction.builder()
                                    .label("Login")
                                    .action("login")
                                    .icon("LogIn")
                                    .build()
                    ))
                    .build();
        }

        List<CartItemDTO> cartItems = cartService.getUserCart(user);

        if (cartItems.isEmpty()) {
            return ChatResponse.builder()
                    .message("Your cart is empty. Start shopping to add items!")
                    .intent("cart_empty")
                    .sessionId(sessionId)
                    .quickActions(List.of(
                            ChatResponse.QuickAction.builder()
                                    .label("Browse Products")
                                    .action("browse_products")
                                    .icon("ShoppingBag")
                                    .build()
                    ))
                    .build();
        }

        double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        return ChatResponse.builder()
                .message(String.format(
                        "🛒 Your cart has %d items\n" +
                                "Total: ₹%,.2f",
                        cartItems.size(),
                        total
                ))
                .intent("cart_found")
                .sessionId(sessionId)
                .cartItems(cartItems)
                .quickActions(List.of(
                        ChatResponse.QuickAction.builder()
                                .label("View Cart")
                                .action("view_cart")
                                .icon("ShoppingCart")
                                .build(),
                        ChatResponse.QuickAction.builder()
                                .label("Checkout")
                                .action("checkout")
                                .icon("CreditCard")
                                .build()
                ))
                .build();
    }

    /**
     * Handle auction queries
     */
    private ChatResponse handleAuctionQuery(User user, String sessionId) {
        List<AuctionDTO> activeAuctions = auctionService.getActiveAuctions();

        if (activeAuctions.isEmpty()) {
            return ChatResponse.builder()
                    .message("There are no active auctions right now. Check back soon for exclusive jewelry auctions!")
                    .intent("auction_none")
                    .sessionId(sessionId)
                    .build();
        }

        return ChatResponse.builder()
                .message(String.format(
                        "🔥 %d live auctions are happening now! Join the bidding to win exclusive jewelry pieces.",
                        activeAuctions.size()
                ))
                .intent("auction_found")
                .sessionId(sessionId)
                .quickActions(List.of(
                        ChatResponse.QuickAction.builder()
                                .label("View Live Auctions")
                                .action("view_auctions")
                                .icon("Gavel")
                                .build()
                ))
                .build();
    }

    /**
     * Handle help queries
     */
    private ChatResponse handleHelpQuery(String sessionId) {
        return ChatResponse.builder()
                .message(
                        "👋 I'm your ShineCart assistant! I can help you with:\n\n" +
                                "• Track your orders\n" +
                                "• Find products\n" +
                                "• View your cart\n" +
                                "• Check live auctions\n\n" +
                                "Just ask me anything!"
                )
                .intent("help")
                .sessionId(sessionId)
                .quickActions(getHelpQuickActions())
                .build();
    }

    /**
     * Handle unknown queries - show contact info
     */
    private ChatResponse handleContactInfo(String sessionId) {
        return ChatResponse.builder()
                .message(
                        "I'm specialized in helping with orders, products, cart, and auctions. " +
                                "For other queries, please contact our support team:"
                )
                .intent("contact")
                .sessionId(sessionId)
                .contactInfo(ChatResponse.ContactInfo.builder()
                        .phone("+91-1234567890")
                        .email("support@shinecart.com")
                        .workingHours("Mon-Sat, 9:00 AM - 6:00 PM")
                        .address("ShineCart HQ, Mumbai, Maharashtra")
                        .build())
                .quickActions(getHelpQuickActions())
                .build();
    }

    // ==================== Helper Methods ====================

    private String extractOrderNumber(String message) {
        // Look for pattern like SC240120001 or #SC240120001
        String[] words = message.split("\\s+");
        for (String word : words) {
            String cleaned = word.replace("#", "").toUpperCase();
            // Match pattern: SC + 9 digits (yyMMdd + 3 digit count)
            if (cleaned.matches("SC\\d{9}")) {
                return cleaned;
            }
        }
        return null;
    }

    private String extractProductSearchTerm(String message) {
        // Remove common query words
        String cleaned = message
                .replaceAll("(find|search|show|looking for|want|get|buy)\\s+", "")
                .replaceAll("(me|some|a|an|the)\\s+", "")
                .trim();

        return cleaned.isEmpty() ? "jewelry" : cleaned;
    }

    private List<ChatResponse.QuickAction> getProductQuickActions() {
        return List.of(
                ChatResponse.QuickAction.builder()
                        .label("Gold Rings")
                        .action("search_product")
                        .icon("Gem")
                        .data("gold rings")
                        .build(),
                ChatResponse.QuickAction.builder()
                        .label("Silver Necklaces")
                        .action("search_product")
                        .icon("Sparkles")
                        .data("silver necklaces")
                        .build(),
                ChatResponse.QuickAction.builder()
                        .label("Diamond Earrings")
                        .action("search_product")
                        .icon("Diamond")
                        .data("diamond earrings")
                        .build()
        );
    }

    private List<ChatResponse.QuickAction> getHelpQuickActions() {
        return List.of(
                ChatResponse.QuickAction.builder()
                        .label("Track Order")
                        .action("track_order")
                        .icon("Package")
                        .build(),
                ChatResponse.QuickAction.builder()
                        .label("Find Products")
                        .action("find_products")
                        .icon("Search")
                        .build(),
                ChatResponse.QuickAction.builder()
                        .label("View Cart")
                        .action("view_cart")
                        .icon("ShoppingCart")
                        .build()
        );
    }
}