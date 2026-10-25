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
public final class EventResponse {
    private final Long id;
    private final Long organizationId;
    private final Long categoryId;
    private final String title;
    private final String description;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String location;
    private final Long capacity;
    private final EventStatus status;
}
