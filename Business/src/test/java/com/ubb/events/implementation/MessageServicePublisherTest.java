package com.ubb.events.implementation;

import com.ubb.events.ChatMessagePublisher;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.User;
import com.ubb.events.dto.MessageResponse;
import com.ubb.events.dto.SendMessageRequest;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.MessageRepository;
import com.ubb.events.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageServicePublisherTest {
    private final MessageRepository messages = mock(MessageRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ChatMessagePublisher publisher = mock(ChatMessagePublisher.class);
    private final MessageServiceImpl service = new MessageServiceImpl(messages, events, users,
            Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC), publisher);

    @Test
    void publishesOnlyAfterMessageIsSaved() {
        Event event = new Event();
        event.setStatus(EventStatus.PUBLISHED);
        User author = new User();
        author.setId(42L);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        when(users.findById(42L)).thenReturn(Optional.of(author));
        when(messages.save(any())).thenAnswer(invocation -> {
            var message = invocation.getArgument(0, com.ubb.events.domain.Message.class);
            message.setId(9L);
            return message;
        });

        MessageResponse response = service.sendMessage(7L, 3L, 42L, new SendMessageRequest("Hello"));

        assertEquals(9L, response.getId());
        verify(publisher).publish(7L, 3L, response);
        verify(messages).save(any());
    }

    @Test
    void doesNotPublishWhenEventCannotReceiveMessage() {
        Event event = new Event();
        event.setStatus(EventStatus.CANCELED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.ubb.events.common.errors.ConflictException.class,
                () -> service.sendMessage(7L, 3L, 42L, new SendMessageRequest("Hello")));

        verifyNoInteractions(publisher, users, messages);
    }
}
