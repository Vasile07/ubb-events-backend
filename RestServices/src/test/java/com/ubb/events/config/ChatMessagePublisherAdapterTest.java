package com.ubb.events.config;

import com.ubb.events.dto.MessageResponse;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.Mockito.*;

class ChatMessagePublisherAdapterTest {
    @Test
    void publishesResponseToEventTopic() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        ChatMessagePublisherAdapter adapter = new ChatMessagePublisherAdapter(template);
        MessageResponse response = new MessageResponse(9L, 42L, "Hello", OffsetDateTime.parse("2026-01-02T03:04:05Z"));

        adapter.publish(7L, 3L, response);

        verify(template).convertAndSend("/topic/organizations/7/events/3/messages", response);
    }
}
