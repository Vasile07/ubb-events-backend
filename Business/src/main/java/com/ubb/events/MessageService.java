package com.ubb.events;

import com.ubb.events.dto.MessageResponse;
import java.util.List;

public interface MessageService {
    List<MessageResponse> listEventMessages(Long organizationId, Long eventId);
}
