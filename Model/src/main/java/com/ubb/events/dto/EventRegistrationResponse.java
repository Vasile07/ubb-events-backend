package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class EventRegistrationResponse {
    private final String firstName;
    private final String lastName;
    private final String email;
}
