package com.example.smartdeskbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * WebSocket bağlantısı için STOMP endpoint'lerini kaydeder.
     * Frontend bu endpoint'e bağlanarak WebSocket iletişimi başlatır.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("🔌 WebSocket: Registering STOMP endpoints...");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*", "https://*.smartdesk.com")
                .withSockJS();
        System.out.println("✅ WebSocket: STOMP endpoint '/ws' registered with SockJS support");
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        System.out.println("🔌 WebSocket: Configuring message broker...");
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
        System.out.println("✅ WebSocket: Message broker configured with prefixes: /topic, /user, /app");
    }
}