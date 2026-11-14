package com.ubb.events.implementation;

import com.ubb.events.MessageService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Message;
import com.ubb.events.domain.User;
import com.ubb.events.dto.MessageResponse;
import com.ubb.events.dto.SendMessageRequest;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.MessageRepository;
import com.ubb.events.persistence.UserRepository;
import java.util.List;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messages;
    private final EventRepository events;
    private final UserRepository users;
    private final Clock clock;

    public MessageServiceImpl(MessageRepository messages, EventRepository events, UserRepository users, Clock clock) {
        this.messages = messages;
        this.events = events;
        this.users = users;
        this.clock = clock;
    }

    @Override
    public List<MessageResponse> listEventMessages(Long organizationId, Long eventId) {
        if (events.findByIdAndOrganizationId(eventId, organizationId).isEmpty()) {
            throw new NotFoundException("Event not found");
        }
        return messages.findAllByEventIdOrderByIdAsc(eventId).stream()
                .map(message -> new MessageResponse(message.getId(), message.getSender().getId(), message.getContent(), message.getSentAt()))
                .toList();
    }

    @Override
    public MessageResponse sendMessage(Long organizationId, Long eventId, Long userId, SendMessageRequest request) {
        if (request == null || request.getContent() == null || request.getContent().isBlank()
                || request.getContent().length() > 1000) {
            throw new BusinessRuleException("Message content must be between 1 and 1000 characters");
        }
        Event event = events.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getStatus() == EventStatus.CANCELED) {
            throw new ConflictException("A canceled event cannot receive messages");
        }
        if (users == null) {
            throw new IllegalStateException("User repository is not configured");
        }
        User author = users.findById(Objects.requireNonNull(userId, "userId"))
                .orElseThrow(() -> new NotFoundException("User not found"));
        Message message = new Message(null, event, author, request.getContent(), OffsetDateTime.now(clock));
        Message saved = messages.save(message);
        return new MessageResponse(saved.getId(), saved.getSender().getId(), saved.getContent(), saved.getSentAt());
    }
}
