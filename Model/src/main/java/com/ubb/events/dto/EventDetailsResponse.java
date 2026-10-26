package com.ubb.events.dto;

import com.ubb.events.domain.EventStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventDetailsResponse(Long id, String title, String description,
        EventOrganizationSummaryResponse organization, LocalDate date, LocalTime startTime,
        LocalTime endTime, String location, Long capacity, Long registrationCount,
        boolean registrationAvailable, EventStatus status, String registrationStatus) { }
