package com.ubb.events.dto;

import com.ubb.events.domain.EventStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class EventDetailsResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final EventOrganizationSummaryResponse organization;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String location;
    private final Long capacity;
    private final Long registrationCount;
    private final boolean registrationAvailable;
    private final EventStatus status;
    private final String registrationStatus;
}
