package com.college.shinecart.service;

import com.college.shinecart.dto.ReportDTO;
import com.college.shinecart.entity.Order;
import com.college.shinecart.entity.OrderItem;
import com.college.shinecart.entity.User;
import com.college.shinecart.repository.OrderRepository;
import com.college.shinecart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // USER REPORT
    public List<ReportDTO.UserReportItem> getUserReport(
            LocalDateTime from, LocalDateTime to) {

        List<User> users = userRepository
                .findByCreatedAtBetween(from, to);

        return users.stream().map(user -> {
            String role = user.getRoles().contains("ROLE_ADMIN")
                    ? "Admin" : "User";
            String status = user.isEnabled() ? "Active" : "Disabled";
            String createdAt = user.getCreatedAt() != null
                    ? user.getCreatedAt().format(DATE_FORMATTER) : "N/A";

            return ReportDTO.UserReportItem.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone() != null ? user.getPhone() : "N/A")
                    .createdAt(createdAt)
                    .status(status)
                    .role(role)
                    .build();
        }).collect(Collectors.toList());
    }

    // ORDER AND REVENUE REPORT
    public ReportDTO.OrderReportResponse getOrderReport(
            LocalDate from, LocalDate to) {

        List<Order> orders = orderRepository
                .findByOrderDateBetween(from, to);

        List<ReportDTO.OrderReportItem> items = orders.stream()
                .map(order -> ReportDTO.OrderReportItem.builder()
                        .orderNumber(order.getOrderNumber())
                        .customerName(order.getShippingAddress() != null
                                && order.getShippingAddress().getName() != null
                                ? order.getShippingAddress().getName()
                                : order.getUser().getUsername())
                        .orderDate(order.getOrderDate().format(DATE_FORMATTER))
                        .deliveryType(order.getDeliveryType()
                                .replace("_", " "))
                        .paymentStatus(order.getPaymentStatus() != null
                                ? order.getPaymentStatus() : "N/A")
                        .status(order.getStatus())
                        .total(order.getTotal())
                        .build())
                .collect(Collectors.toList());

        Double totalRevenue = orders.stream()
                .filter(o -> "SUCCESS".equals(o.getPaymentStatus())
                        && "Delivered".equals(o.getStatus()))
                .mapToDouble(Order::getTotal)
                .sum();

        return ReportDTO.OrderReportResponse.builder()
                .orders(items)
                .totalOrders(items.size())
                .totalRevenue(totalRevenue)
                .build();
    }

    // PRODUCT PERFORMANCE REPORT
    public List<ReportDTO.ProductReportItem> getProductReport(
            LocalDate from, LocalDate to) {

        List<Order> orders = orderRepository
                .findByOrderDateBetween(from, to)
                .stream()
                .filter(o -> "SUCCESS".equals(o.getPaymentStatus())
                        && "Delivered".equals(o.getStatus()))
                .collect(Collectors.toList());

        Map<Long, ReportDTO.ProductReportItem> productMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                Long productId = item.getProduct().getId();

                if (productMap.containsKey(productId)) {
                    ReportDTO.ProductReportItem existing =
                            productMap.get(productId);
                    existing.setTotalQuantitySold(
                            existing.getTotalQuantitySold()
                                    + item.getQuantity());
                    existing.setTotalRevenue(
                            existing.getTotalRevenue()
                                    + (item.getPrice() * item.getQuantity()));
                } else {
                    productMap.put(productId,
                            ReportDTO.ProductReportItem.builder()
                                    .productName(item.getName())
                                    .category(item.getProduct().getCategory())
                                    .metal(item.getProduct().getMetal())
                                    .totalQuantitySold(item.getQuantity())
                                    .totalRevenue(
                                            item.getPrice() * item.getQuantity())
                                    .build());
                }
            }
        }

        return productMap.values().stream()
                .sorted(Comparator.comparingDouble(
                                ReportDTO.ProductReportItem::getTotalRevenue)
                        .reversed())
                .collect(Collectors.toList());
    }
}