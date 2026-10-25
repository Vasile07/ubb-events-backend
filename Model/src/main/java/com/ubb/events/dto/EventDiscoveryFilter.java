package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class EventDiscoveryFilter {
    private final String search;
    private final Long organizationId;
    private final LocalDate from;
    private final LocalDate to;
}
