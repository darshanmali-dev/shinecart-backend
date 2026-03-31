package com.college.shinecart.service;

import com.college.shinecart.dto.UserDTO;
import com.college.shinecart.entity.Order;
import com.college.shinecart.entity.User;
import com.college.shinecart.exception.ResourceNotFoundException;
import com.college.shinecart.repository.OrderRepository;
import com.college.shinecart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    public void sendForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("No account found with this email"));

        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);
    }

    // Verify OTP
    public boolean verifyForgotPasswordOtp(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }

    // Reset password
    public void resetPassword(String email, String otp,
                              String newPassword) {
        if (!otpService.verifyOtp(email, otp)) {
            throw new RuntimeException(
                    "Invalid or expired OTP. Please try again.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpService.clearOtp(email);
    }

    /**
     * Get all users (admin)
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return toUserDTO(user);
    }

    /**
     * Toggle user enabled/disabled
     */
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return toUserDTO(user);
    }

    /**
     * Get user stats (admin)
     */
    public Map<String, Object> getUserStats() {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isEnabled).count();
        long adminUsers = allUsers.stream()
                .filter(u -> u.getRoles().contains("ROLE_ADMIN"))
                .count();
        long regularUsers = allUsers.stream()
                .filter(u -> u.getRoles().contains("ROLE_USER"))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("regularUsers", regularUsers);
        return stats;
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO toUserDTO(User user) {
        // Get order stats for this user
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long totalOrders = userOrders.size();
        double totalSpent = userOrders.stream()
                .filter(o -> "Delivered".equals(o.getStatus()))
                .mapToDouble(Order::getTotal)
                .sum();

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .build();
    }
}