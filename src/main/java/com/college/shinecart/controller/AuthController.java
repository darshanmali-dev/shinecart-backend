package com.college.shinecart.controller;

import com.college.shinecart.dto.AuthResponse;
import com.college.shinecart.dto.LoginRequest;
import com.college.shinecart.dto.RegisterRequest;
import com.college.shinecart.entity.User;
import com.college.shinecart.service.AuthService;
import com.college.shinecart.service.EmailService;
import com.college.shinecart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")  // Your frontend URL
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }



    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();

            String role = user.getRoles().get(0)
                    .replace("ROLE_", "")
                    .toLowerCase();

            AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.builder()
                    .id(String.valueOf(user.getId()))
                    .name(user.getUsername())
                    .email(user.getEmail())
                    .role(role)
                    .avatar(null)
                    .build();

            return ResponseEntity.ok(userDTO);
        }

        return ResponseEntity.status(401).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(
            @RequestBody Map<String, String> request) {
        try {
            userService.sendForgotPasswordOtp(request.get("email"));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP sent to your email address"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody Map<String, String> request) {
        boolean valid = userService.verifyForgotPasswordOtp(
                request.get("email"), request.get("otp"));
        return ResponseEntity.ok(Map.of(
                "success", valid,
                "message", valid
                        ? "OTP verified successfully"
                        : "Invalid or expired OTP"
        ));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> request) {
        try {
            userService.resetPassword(
                    request.get("email"),
                    request.get("otp"),
                    request.get("newPassword")
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password reset successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}