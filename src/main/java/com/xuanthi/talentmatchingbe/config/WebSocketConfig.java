package com.xuanthi.talentmatchingbe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Mở cổng "/ws-chat" để Frontend kết nối vào.
        // withSockJS() giúp tương thích với các trình duyệt cũ không hỗ trợ WebSocket thuần.
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // Cho phép Frontend (React/Vue) gọi vào
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. Tiền tố cho các luồng dữ liệu Backend đẩy về Frontend
        // Vì chúng ta làm Chat 1-1 (Private Chat), nên ta dùng "/user"
        registry.enableSimpleBroker("/user");

        // 3. Tiền tố khi Frontend gửi tin nhắn lên Backend (Ví dụ: Frontend sẽ gửi vào /app/chat)
        registry.setApplicationDestinationPrefixes("/app");

        // 4. Tiền tố định danh người dùng cụ thể
        registry.setUserDestinationPrefix("/user");
    }
}