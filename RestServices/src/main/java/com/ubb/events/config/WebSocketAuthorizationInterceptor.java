package com.ubb.events.config;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.persistence.EventRepository;
import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public final class WebSocketAuthorizationInterceptor implements ChannelInterceptor {
    private final JwtTokenService tokenService;
    private final OrganizationAuthorizationService authorizationService;
    private final EventRepository eventRepository;
    private final ConcurrentHashMap<String, UserPrincipal> sessions = new ConcurrentHashMap<>();

    public WebSocketAuthorizationInterceptor(JwtTokenService tokenService, OrganizationAuthorizationService authorizationService) {
        this(tokenService, authorizationService, null);
    }

    public WebSocketAuthorizationInterceptor(JwtTokenService tokenService, OrganizationAuthorizationService authorizationService,
            EventRepository eventRepository) {
        this.tokenService = tokenService;
        this.authorizationService = authorizationService;
        this.eventRepository = eventRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) return null;
            try {
                Long userId = tokenService.parseUserId(header.substring(7));
                UserPrincipal principal = new UserPrincipal(userId);
                accessor.setUser(principal);
                if (accessor.getSessionId() != null) sessions.put(accessor.getSessionId(), principal);
                return message;
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (principal == null && accessor.getSessionId() != null) principal = sessions.get(accessor.getSessionId());
            if (principal == null) {
                String header = accessor.getFirstNativeHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    try { principal = new UserPrincipal(tokenService.parseUserId(header.substring(7))); }
                    catch (RuntimeException ignored) { return null; }
                }
            }
            TopicScope scope = topicScope(accessor.getDestination());
            if (!(principal instanceof UserPrincipal user) || scope == null
                    || !authorizationService.hasAtLeast(user.userId(), scope.organizationId(), EffectiveOrgRole.MEMBER)) return null;
            if (eventRepository != null && eventRepository.findByIdAndOrganizationId(scope.eventId(), scope.organizationId()).isEmpty()) return null;
        }
        return message;
    }

    private TopicScope topicScope(String destination) {
        if (destination == null || !destination.startsWith("/topic/organizations/")) return null;
        String[] parts = destination.substring("/topic/organizations/".length()).split("/");
        if (parts.length != 4 || !"events".equals(parts[1]) || !"messages".equals(parts[3])) return null;
        try { return new TopicScope(Long.valueOf(parts[0]), Long.valueOf(parts[2])); }
        catch (NumberFormatException ignored) { return null; }
    }

    private record TopicScope(Long organizationId, Long eventId) { }

    record UserPrincipal(Long userId) implements Principal {
        @Override public String getName() { return userId.toString(); }
    }
}
