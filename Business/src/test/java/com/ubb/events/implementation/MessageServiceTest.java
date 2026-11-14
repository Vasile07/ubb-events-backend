package com.ubb.events.implementation;

import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Message;
import com.ubb.events.domain.User;
import com.ubb.events.dto.MessageResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.MessageRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MessageServiceTest {
    private final MessageRepository messages = mock(MessageRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final MessageServiceImpl service = new MessageServiceImpl(messages, events);

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
}
