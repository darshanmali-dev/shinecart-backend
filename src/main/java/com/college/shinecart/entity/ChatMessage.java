package com.college.shinecart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;

    private String sessionId;  // Track conversation

    @ManyToOne
    private User user;  // Nullable for guests

    private String message;  // User's message
    private String response;  // Bot's response
    private String intent;  // Detected intent (order, product, cart, etc.)
    private LocalDateTime timestamp;
}