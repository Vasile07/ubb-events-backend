package com.ubb.events.implementation;

import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.persistence.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);
    private static final LocalTime START = LocalTime.of(18, 0);

    @Mock EventRepository events;
    @InjectMocks EventServiceImpl service;

    private final Organization organization = new Organization(7L, "Students", null, null);

    @Test
    void listsAllEventsOfOrganizationIncludingDraftAndCanceled() {
        Event draft = new Event(1L, organization, 3L, "Hackathon", "Coding night",
                DATE, START, LocalTime.of(22, 0), "Room 1", 50L, EventStatus.DRAFT);
        Event published = new Event(2L, organization, 3L, "Workshop", "Spring Boot",
                DATE.plusDays(1), START, null, null, null, EventStatus.PUBLISHED);
        Event canceled = new Event(3L, organization, 4L, "Party", "Canceled party",
                DATE.plusDays(2), START, null, null, null, EventStatus.CANCELED);
        when(events.findAllByOrganizationIdOrderByDateAscStartTimeAsc(7L)).thenReturn(List.of(draft, published, canceled));

        List<EventResponse> result = service.listOrganizationEvents(7L);

        assertEquals(List.of(
                new EventResponse(1L, 7L, 3L, "Hackathon", "Coding night",
                        DATE, START, LocalTime.of(22, 0), "Room 1", 50L, EventStatus.DRAFT),
                new EventResponse(2L, 7L, 3L, "Workshop", "Spring Boot",
                        DATE.plusDays(1), START, null, null, null, EventStatus.PUBLISHED),
                new EventResponse(3L, 7L, 4L, "Party", "Canceled party",
                        DATE.plusDays(2), START, null, null, null, EventStatus.CANCELED)), result);
        verify(events).findAllByOrganizationIdOrderByDateAscStartTimeAsc(7L);
    }

    @Test
    void returnsEmptyListWhenOrganizationHasNoEvents() {
        when(events.findAllByOrganizationIdOrderByDateAscStartTimeAsc(7L)).thenReturn(List.of());

        assertTrue(service.listOrganizationEvents(7L).isEmpty());
        verify(events).findAllByOrganizationIdOrderByDateAscStartTimeAsc(7L);
    }

    @Test
    void returnsEventOfOrganization() {
        Event event = new Event(1L, organization, 3L, "Hackathon", "Coding night",
                DATE, START, null, "Room 1", 50L, EventStatus.DRAFT);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(event));

        assertEquals(new EventResponse(1L, 7L, 3L, "Hackathon", "Coding night",
                DATE, START, null, "Room 1", 50L, EventStatus.DRAFT), service.getOrganizationEvent(7L, 1L));
    }

    @Test
    void throwsNotFoundForMissingEventOrEventOfAnotherOrganization() {
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.getOrganizationEvent(7L, 1L));
        assertEquals("Event not found", exception.getMessage());
        verify(events).findByIdAndOrganizationId(1L, 7L);
    }
}
