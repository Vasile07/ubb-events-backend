package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class EventTaskResponse {
    private final Long id;
    private final String description;
    private final TaskAssigneeResponse assignee;
}
