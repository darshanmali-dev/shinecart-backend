package com.college.shinecart.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String sessionId;  // To track conversation context
    private String message;    // User's message
    private Map<String, String> context;  // Optional: for follow-up questions
}