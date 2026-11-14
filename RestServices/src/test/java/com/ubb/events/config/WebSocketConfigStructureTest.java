package com.ubb.events.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebSocketConfigStructureTest {
    @Test
    void enablesStompBrokerAndDeclaresExpectedConfiguration() throws Exception {
        assertTrue(WebSocketConfig.class.isAnnotationPresent(EnableWebSocketMessageBroker.class));
        assertNotNull(WebSocketConfig.class.getDeclaredMethod("configureMessageBroker", MessageBrokerRegistry.class));
        assertNotNull(WebSocketConfig.class.getDeclaredMethod("registerStompEndpoints", StompEndpointRegistry.class));
    }
}
