package com.ubb.events.dto;

import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
public class MessageResponse {
    private final Long id;
    private final Long senderId;
    private final String content;
    private final OffsetDateTime sentAt;

    public MessageResponse(Long id, Long senderId, String content) {
        this(id, senderId, content, null);
    }

    public MessageResponse(Long id, Long senderId, String content, OffsetDateTime sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
    }
}
