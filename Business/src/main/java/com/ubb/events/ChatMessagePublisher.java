package com.ubb.events;

import com.ubb.events.dto.MessageResponse;

/** Publishes a successfully persisted chat message to delivery adapters. */
public interface ChatMessagePublisher {
    void publish(Long organizationId, Long eventId, MessageResponse message);
}
