package com.college.shinecart.service;

import com.college.shinecart.dto.CreateOrderRequest;
import com.college.shinecart.dto.OrderResponse;
import com.college.shinecart.entity.*;
import com.college.shinecart.exception.BadRequestException;
import com.college.shinecart.repository.OrderRepository;
import com.college.shinecart.repository.ProductRepository;
import com.college.shinecart.repository.StoreRepository;
import com.college.shinecart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;



    /**
     * Get all orders (admin)
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get order stats (admin)
     */
    public Map<String, Object> getOrderStats() {
        List<Order> allOrders = orderRepository.findAll();

        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> "Order Placed".equals(o.getStatus())).count();
        long processingOrders = allOrders.stream().filter(o -> "Processing".equals(o.getStatus())).count();
        long shippedOrders = allOrders.stream().filter(o -> "Shipped".equals(o.getStatus())).count();
        long inTransitOrders = allOrders.stream().filter(o -> "In Transit".equals(o.getStatus())).count();
        long deliveredOrders = allOrders.stream().filter(o -> "Delivered".equals(o.getStatus())).count();
        long failedOrders = allOrders.stream().filter(o -> "Order Failed".equals(o.getStatus())).count();
        long cancelledOrders = allOrders.stream().filter(o -> "Cancelled".equals(o.getStatus())).count();
        double totalRevenue = allOrders.stream()
                .filter(o -> "Delivered".equals(o.getStatus())) // Only count delivered orders as revenue
                .mapToDouble(Order::getTotal)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("processingOrders", processingOrders);
        stats.put("shippedOrders", shippedOrders);
        stats.put("inTransitOrders", inTransitOrders);
        stats.put("deliveredOrders", deliveredOrders);
        stats.put("failedOrders", failedOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }
    /**
     * Create a new order (called when user clicks "Proceed to Checkout")
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request, Long userId) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));



        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status("Order Placed")
                .orderDate(LocalDate.now())
                .expectedDelivery(LocalDate.now().plusDays(5))  // 5 days from now
                .deliveryType(request.getDeliveryType())
                .paymentStatus("PENDING")
                .build();

        // Calculate total and add items
        double total = 0.0;
        for (CreateOrderRequest.OrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDTO.getProductId()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .name(product.getName())
                    .price(product.getPrice())
                    .quantity(itemDTO.getQuantity())
                    .image(product.getImage())
                    .size(itemDTO.getSize())
                    .build();

            if (product.getStockQuantity() != null) {
                int updatedStock = product.getStockQuantity() - itemDTO.getQuantity();
                if (updatedStock < 0) {
                    throw new BadRequestException(
                            "Insufficient stock for product: " + product.getName());
                }
                product.setStockQuantity(updatedStock);
                productRepository.save(product);
            }
            order.addItem(orderItem);
            total += product.getPrice() * itemDTO.getQuantity();
        }
        order.setTotal(total);

        // Handle delivery type
        if ("HOME_DELIVERY".equals(request.getDeliveryType())) {
            // Set shipping address
            ShippingAddress address = ShippingAddress.builder()
                    .name(request.getShippingAddress().getName())
                    .address(request.getShippingAddress().getAddress())
                    .city(request.getShippingAddress().getCity())
                    .state(request.getShippingAddress().getState())
                    .pincode(request.getShippingAddress().getPincode())
                    .phone(request.getShippingAddress().getPhone())
                    .email(request.getShippingAddress().getEmail())
                    .landmark(request.getShippingAddress().getLandmark())
                    .build();
            order.setShippingAddress(address);
        } else if ("STORE_PICKUP".equals(request.getDeliveryType())) {
            // Set store pickup details
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found"));
            order.setPickupStoreName(store.getName());
            order.setPickupStoreAddress(store.getAddress());
            order.setPickupStoreCity(store.getCity());
            order.setPickupStorePhone(store.getPhone());
        }

        // Add initial tracking step
        TrackingStep initialStep = TrackingStep.builder()
                .status("Order Placed")
                .date(LocalDate.now())
                .time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a")))
                .description("Your order has been confirmed and is being prepared")
                .completed(true)
                .current(true)
                .icon("CheckCircle")
                .build();
        order.addTrackingStep(initialStep);

        // Save order
        return orderRepository.save(order);
    }

    /**
     * Generate unique order number (format: SC240120001)
     */
    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long count = orderRepository.count() + 1;
        return String.format("SC%s%03d", datePrefix, count);
    }

    /**
     * Get order by order number
     */
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
    }

    /**
     * Get all orders for a user
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Convert Order entity to OrderResponse DTO
     */
    public OrderResponse toOrderResponse(Order order) {
        OrderResponse response = OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .orderDate(order.getOrderDate().toString())
                .expectedDelivery(order.getExpectedDelivery().toString())
                .total(order.getTotal())
                .deliveryType(order.getDeliveryType())
                .userId(order.getUser().getId())           // ADD THIS
                .userName(order.getUser().getUsername())    // ADD THIS
                .userEmail(order.getUser().getEmail())
                .build();

        // Add items
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .image(item.getImage())
                        .build())
                .collect(Collectors.toList());
        response.setItems(items);

        // Add shipping address or store pickup
        if ("HOME_DELIVERY".equals(order.getDeliveryType()) && order.getShippingAddress() != null) {
            ShippingAddress addr = order.getShippingAddress();
            response.setShippingAddress(OrderResponse.ShippingAddressResponse.builder()
                    .name(addr.getName())
                    .address(addr.getAddress())
                    .city(addr.getCity())
                    .state(addr.getState())
                    .pincode(addr.getPincode())
                    .phone(addr.getPhone())
                    .build());
        } else if ("STORE_PICKUP".equals(order.getDeliveryType())) {
            response.setStorePickup(OrderResponse.StorePickupResponse.builder()
                    .storeName(order.getPickupStoreName())
                    .storeAddress(order.getPickupStoreAddress())
                    .storeCity(order.getPickupStoreCity())
                    .storePhone(order.getPickupStorePhone())
                    .build());
        }

        // Add tracking steps
        List<OrderResponse.TrackingStepResponse> steps = order.getTrackingSteps().stream()
                .map(step -> OrderResponse.TrackingStepResponse.builder()
                        .status(step.getStatus())
                        .date(step.getDate().toString())
                        .time(step.getTime())
                        .description(step.getDescription())
                        .completed(step.getCompleted())
                        .current(step.getCurrent())
                        .icon(step.getIcon())
                        .build())
                .collect(Collectors.toList());
        response.setTrackingSteps(steps);

        return response;
    }

    /**
     * Update order status and add tracking step
     */
    @Transactional
    public void updateOrderStatus(String orderNumber, String newStatus, String description) {
        Order order = getOrderByOrderNumber(orderNumber);

        // Mark all tracking steps as not current
        order.getTrackingSteps().forEach(step -> step.setCurrent(false));

        // Update order status
        order.setStatus(newStatus);

        if (newStatus.equals("Cancelled") &&
                "SUCCESS".equals(order.getPaymentStatus())) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getStockQuantity() != null) {
                    product.setStockQuantity(
                            product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        if(newStatus.equals("Delivered")) {
            order.setExpectedDelivery(LocalDate.now());
        }
        // Add new tracking step
        TrackingStep newStep = TrackingStep.builder()
                .status(newStatus)
                .date(LocalDate.now())
                .time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a")))
                .description(description)
                .completed(true)
                .current(true)
                .icon(getIconForStatus(newStatus))
                .build();
        order.addTrackingStep(newStep);

        orderRepository.save(order);


    }

    /**
     * Get icon based on status
     */
    private String getIconForStatus(String status) {
        return switch (status) {
            case "Order Placed" -> "CheckCircle";
            case "Processing" -> "Package";
            case "Shipped" -> "Truck";
            case "In Transit" -> "MapPin";
            case "Delivered" -> "CheckCircle";
            default -> "Package";
        };
    }
}