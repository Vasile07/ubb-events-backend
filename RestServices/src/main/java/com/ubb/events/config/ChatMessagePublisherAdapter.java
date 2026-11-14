package com.ubb.events.config;

import com.ubb.events.ChatMessagePublisher;
import com.ubb.events.dto.MessageResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMessagePublisherAdapter implements ChatMessagePublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessagePublisherAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(Long organizationId, Long eventId, MessageResponse message) {
        messagingTemplate.convertAndSend(topic(organizationId, eventId), message);
    }

    public static String topic(Long organizationId, Long eventId) {
        return "/topic/organizations/" + organizationId + "/events/" + eventId + "/messages";
    }
}
