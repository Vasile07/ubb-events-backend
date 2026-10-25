package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Setter
public final class EventTaskRequest {
    private String description;
    private Long assigneeUserId;
}
