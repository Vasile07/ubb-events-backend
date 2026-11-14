package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResponse {
    private final Long id;
    private final Long senderId;
    private final String content;
}
