package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrationDetailsResponse {
    private final Long registrationId;
    private final EventSummaryResponse event;
}
