package com.ubb.events;

import com.ubb.events.dto.MessageResponse;
import com.ubb.events.dto.SendMessageRequest;
import java.util.List;

public interface MessageService {
    List<MessageResponse> listEventMessages(Long organizationId, Long eventId);
    MessageResponse sendMessage(Long organizationId, Long eventId, Long userId, SendMessageRequest request);
}
