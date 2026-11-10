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
public final class EventSummaryResponse {
    private final Long id;
    private final String title;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String location;
    private final EventStatus status;
    private final EventOrganizationSummaryResponse organization;
    private final Long capacity;
    private final Long spotsTaken;

    public EventSummaryResponse(Long id, String title, LocalDate date, LocalTime startTime, LocalTime endTime,
            String location, EventStatus status, EventOrganizationSummaryResponse organization) {
        this(id, title, date, startTime, endTime, location, status, organization, null, null);
    }
}
