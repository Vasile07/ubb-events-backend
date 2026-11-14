package com.ubb.events.config;

import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.OrganizationRepository;
import com.ubb.events.persistence.UserRepository;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSocketDeliveryIntegrationTest {
    @LocalServerPort int port;
    @Autowired JwtTokenService tokenService;
    @Autowired SimpUserRegistry userRegistry;
    @Autowired MockMvc mockMvc;
    @Autowired OrganizationRepository organizationRepository;
    @Autowired UserRepository userRepository;
    @Autowired MembershipRepository membershipRepository;
    @Autowired EventRepository eventRepository;
    @MockitoSpyBean ChatMessagePublisherAdapter publisherAdapter;
    private WebSocketStompClient client;
    private final LinkedBlockingQueue<Throwable> clientErrors = new LinkedBlockingQueue<>();

    @AfterEach
    void closeClient() {
        if (client != null) client.stop();
    }

    @Test
    void memberAndManagerReceiveNewlyPublishedEventMessage() throws Exception {
        Organization organization = organizationRepository.save(new Organization(null, "Chat org", "", null));
        User member = userRepository.save(new User(null, "Member", "User", "member@example.com", "pw"));
        User manager = userRepository.save(new User(null, "Manager", "User", "manager@example.com", "pw"));
        membershipRepository.save(new Membership(null, member, organization, MembershipRole.MEMBER, MembershipStatus.ACTIVE));
        membershipRepository.save(new Membership(null, manager, organization, MembershipRole.MANAGER, MembershipStatus.ACTIVE));
        Event event = eventRepository.save(new Event(null, organization, 1L, "Chat", "Chat", java.time.LocalDate.now(),
                java.time.LocalTime.NOON, java.time.LocalTime.NOON.plusHours(1), "Room", 20L, EventStatus.PUBLISHED));
        client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        client.start();
        CountDownLatch received = new CountDownLatch(2);
        LinkedBlockingQueue<JsonNode> payloads = new LinkedBlockingQueue<>();
        StompFrameHandler handler = new StompFrameHandler() {
            @Override public java.lang.reflect.Type getPayloadType(StompHeaders headers) { return JsonNode.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) {
                payloads.add((JsonNode) payload);
                received.countDown();
            }
        };
        subscribeAs(member.getId(), organization.getId(), event.getId(), handler);
        subscribeAs(manager.getId(), organization.getId(), event.getId(), handler);
        String destination = "/topic/organizations/" + organization.getId() + "/events/" + event.getId() + "/messages";
        awaitSubscription(member.getId(), destination);
        awaitSubscription(manager.getId(), destination);
        assertEquals(2, userRegistry.getUserCount(), "Both authenticated STOMP sessions should be registered");
        mockMvc.perform(post("/api/organizations/{organizationId}/events/{eventId}/messages", organization.getId(), event.getId())
                        .header("Authorization", "Bearer " + tokenService.issue(member.getId()))
                        .contentType(APPLICATION_JSON).content("{\"content\":\"Hello\"}"))
                .andExpect(status().isCreated());
        verify(publisherAdapter).publish(eq(organization.getId()), eq(event.getId()), any());

        assertTrue(received.await(3, TimeUnit.SECONDS), "No STOMP payload received; client errors=" + clientErrors);
        assertEquals(0, received.getCount());
        List<JsonNode> delivered = List.of(payloads.poll(), payloads.poll());
        assertTrue(delivered.stream().allMatch(payload -> payload.path("content").asText().equals("Hello")
                && payload.path("senderId").asLong() == member.getId()
                && payload.path("id").asLong() > 0),
                "Expected the REST message response payload");
    }

    private void subscribeAs(Long userId, Long organizationId, Long eventId, StompFrameHandler handler) throws Exception {
        StompHeaders connect = new StompHeaders();
        connect.add("Authorization", "Bearer " + tokenService.issue(userId));
        StompSession session = client.connectAsync("ws://localhost:" + port + "/ws", new WebSocketHttpHeaders(), connect,
                new StompSessionHandlerAdapter() {
                    @Override public void handleException(StompSession session, StompCommand command, StompHeaders headers,
                            byte[] payload, Throwable exception) {
                        clientErrors.add(exception);
                    }
                    @Override public void handleTransportError(StompSession session, Throwable exception) {
                        clientErrors.add(exception);
                    }
                }).get(5, TimeUnit.SECONDS);
        StompHeaders subscription = new StompHeaders();
        subscription.setDestination("/topic/organizations/" + organizationId + "/events/" + eventId + "/messages");
        subscription.add("Authorization", "Bearer " + tokenService.issue(userId));
        session.subscribe(subscription, handler);
    }

    private void awaitSubscription(Long userId, String destination) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            var user = userRegistry.getUser(userId.toString());
            if (user != null && user.getSessions().stream().flatMap(session -> session.getSubscriptions().stream())
                    .anyMatch(subscription -> destination.equals(subscription.getDestination()))) {
                return;
            }
            Thread.sleep(25);
        }
        assertTrue(false, "STOMP subscription was not registered for user " + userId + " at " + destination);
    }
}
