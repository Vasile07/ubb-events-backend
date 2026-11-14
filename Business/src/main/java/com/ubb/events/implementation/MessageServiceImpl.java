package com.ubb.events.implementation;

import com.ubb.events.MessageService;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.dto.MessageResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.MessageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messages;
    private final EventRepository events;

    public MessageServiceImpl(MessageRepository messages, EventRepository events) {
        this.messages = messages;
        this.events = events;
    }

    @Override
    public List<MessageResponse> listEventMessages(Long organizationId, Long eventId) {
        if (events.findByIdAndOrganizationId(eventId, organizationId).isEmpty()) {
            throw new NotFoundException("Event not found");
        }
        return messages.findAllByEventIdOrderByIdAsc(eventId).stream()
                .map(message -> new MessageResponse(message.getId(), message.getSender().getId(), message.getContent()))
                .toList();
    }
}
