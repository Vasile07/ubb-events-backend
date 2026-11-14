package com.ubb.events.implementation;

import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.domain.Message;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.User;
import com.ubb.events.dto.MessageResponse;
import com.ubb.events.dto.SendMessageRequest;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.MessageRepository;
import com.ubb.events.persistence.UserRepository;
import java.util.List;
import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MessageServiceTest {
    private final MessageRepository messages = mock(MessageRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final MessageServiceImpl service = new MessageServiceImpl(messages, events, users,
            Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC));

    @Test
    void listsMessagesAsResponsesInRepositoryOrder() {
        User sender = new User();
        sender.setId(8L);
        Message first = new Message();
        first.setId(2L); first.setSender(sender); first.setContent("Second");
        Message second = new Message();
        second.setId(4L); second.setSender(sender); second.setContent("Fourth");
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(new com.ubb.events.domain.Event()));
        when(messages.findAllByEventIdOrderByIdAsc(3L)).thenReturn(List.of(first, second));

        List<MessageResponse> result = service.listEventMessages(7L, 3L);

        assertEquals(List.of(2L, 4L), result.stream().map(MessageResponse::getId).toList());
        assertEquals(8L, result.get(0).getSenderId());
        assertEquals("Second", result.get(0).getContent());
    }

    @Test
    void rejectsEventOutsideOrganization() {
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.listEventMessages(7L, 3L));
        verifyNoInteractions(messages);
    }

    @Test
    void sendsMessageUsingAuthenticatedAuthorAndCurrentTime() {
        User author = new User(); author.setId(42L);
        Event event = new Event(); event.setStatus(EventStatus.PUBLISHED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        when(users.findById(42L)).thenReturn(Optional.of(author));
        when(messages.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(9L);
            return message;
        });
        MessageResponse result = service.sendMessage(7L, 3L, 42L, new SendMessageRequest("Hello"));

        assertEquals(9L, result.getId());
        assertEquals(42L, result.getSenderId());
        assertEquals("Hello", result.getContent());
        assertEquals("2026-01-02T03:04:05Z", result.getSentAt().toString());
        verify(messages).save(argThat(message -> message.getSender() == author && message.getEvent() == event));
    }

    @Test
    void rejectsCanceledEvent() {
        Event event = new Event(); event.setStatus(EventStatus.CANCELED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.sendMessage(7L, 3L, 42L, new SendMessageRequest("Hello")));
        assertEquals("A canceled event cannot receive messages", exception.getMessage());
        verifyNoInteractions(users, messages);
    }

    @Test
    void rejectsBlankAndOverlongContent() {
        assertThrows(com.ubb.events.common.errors.BusinessRuleException.class,
                () -> service.sendMessage(7L, 3L, 42L, new SendMessageRequest("  ")));
        assertThrows(com.ubb.events.common.errors.BusinessRuleException.class,
                () -> service.sendMessage(7L, 3L, 42L, new SendMessageRequest("x".repeat(1001))));
        verifyNoInteractions(events, users, messages);
    }
}
