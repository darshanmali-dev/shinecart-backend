package com.college.shinecart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker
     * - /topic is used for broadcasting messages (one-to-many)
     * - /app is the prefix for messages bound for @MessageMapping methods
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker with destination prefix /topic
        registry.enableSimpleBroker("/topic");

        // Set application destination prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register STOMP endpoints
     * This is the endpoint that clients will connect to
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Allow all origins (adjust for production)
                .withSockJS();  // Enable SockJS fallback for browsers without WebSocket support
    }
}