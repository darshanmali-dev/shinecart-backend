package com.college.shinecart.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketTestController {

    /**
     * Test endpoint - clients send to /app/test
     * Server broadcasts to /topic/test
     */
    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String testWebSocket(String message) {
        return "Echo: " + message;
    }
}