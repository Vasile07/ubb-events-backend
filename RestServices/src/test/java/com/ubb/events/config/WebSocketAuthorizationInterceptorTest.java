package com.ubb.events.config;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.persistence.EventRepository;
import java.util.Optional;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class WebSocketAuthorizationInterceptorTest {
    private final JwtTokenService tokens = mock(JwtTokenService.class);
    private final OrganizationAuthorizationService authorization = mock(OrganizationAuthorizationService.class);
    private final EventRepository events = mock(EventRepository.class);
    private final WebSocketAuthorizationInterceptor interceptor = new WebSocketAuthorizationInterceptor(tokens, authorization, events);

    @Test
    void authenticatesConnectUsingBearerToken() {
        when(tokens.parseUserId("token")).thenReturn(42L);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer token");
        accessor.setLeaveMutable(true);

        Message<?> connectMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(connectMessage, null);

        assertSame(connectMessage, result);
        assertNotNull(MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class).getUser());
    }

    @Test
    void rejectsUnauthenticatedAndNonMemberSubscriptions() {
        StompHeaderAccessor noAuth = StompHeaderAccessor.create(StompCommand.CONNECT);
        assertNull(interceptor.preSend(MessageBuilder.createMessage(new byte[0], noAuth.getMessageHeaders()), null));

        StompHeaderAccessor subscribe = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        subscribe.setDestination("/topic/organizations/7/events/3/messages");
        subscribe.setUser((Principal) () -> "42");
        when(authorization.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(false);
        assertNull(interceptor.preSend(MessageBuilder.createMessage(new byte[0], subscribe.getMessageHeaders()), null));
    }
}
