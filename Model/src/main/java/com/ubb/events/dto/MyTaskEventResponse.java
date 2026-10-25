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
public final class MyTaskEventResponse {
    private final Long id;
    private final String title;
    private final LocalDate date;
    private final LocalTime startTime;
    private final EventStatus status;
}
