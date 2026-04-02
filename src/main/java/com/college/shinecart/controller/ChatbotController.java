package com.college.shinecart.controller;

import com.college.shinecart.dto.ChatRequest;
import com.college.shinecart.dto.ChatResponse;
import com.college.shinecart.entity.User;
import com.college.shinecart.repository.UserRepository;
import com.college.shinecart.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final UserRepository userRepository;

    /**
     * Process chat message
     * POST /api/chatbot/message
     */
    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody ChatRequest request) {
        try {
            // Get user (nullable for guests)
            User user = getAuthenticatedUser();

            ChatResponse response = chatbotService.processMessage(request, user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log the error
            System.err.println("Chatbot error: " + e.getMessage());
            e.printStackTrace();

            // Return error response
            Map<String, String> error = new HashMap<>();
            error.put("message", "Sorry, I encountered an error. Please try again.");
            error.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint
     * GET /api/chatbot/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Chatbot service is running");
        return ResponseEntity.ok(response);
    }

    /**
     * Get authenticated user (returns null if not logged in)
     */
    private User getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser")) {
                String name = authentication.getName();
                return userRepository.findByUsername(name).orElse(null);
            }
        } catch (Exception e) {
            // Guest user - no authentication
            System.out.println("No authenticated user: " + e.getMessage());
        }
        return null;
    }
}