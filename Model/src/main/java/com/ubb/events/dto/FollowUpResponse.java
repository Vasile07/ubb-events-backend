package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowUpResponse {
    private final Long id;
    private final Long eventId;
    private final String content;
}
