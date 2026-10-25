package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class MyTaskResponse {
    private final Long id;
    private final String description;
    private final MyTaskEventResponse event;
    private final MyTaskOrganizationResponse organization;
}
