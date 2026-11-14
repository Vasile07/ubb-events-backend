package com.ubb.events.config;

import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.persistence.EventRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenService tokenService;
    private final OrganizationAuthorizationService authorizationService;
    private final EventRepository eventRepository;

    public WebSocketConfig(JwtTokenService tokenService, OrganizationAuthorizationService authorizationService,
            EventRepository eventRepository) {
        this.tokenService = tokenService;
        this.authorizationService = authorizationService;
        this.eventRepository = eventRepository;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:5173");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthorizationInterceptor());
    }

    @Bean
    WebSocketAuthorizationInterceptor webSocketAuthorizationInterceptor() {
        return new WebSocketAuthorizationInterceptor(tokenService, authorizationService, eventRepository);
    }
}
