package com.ubb.events.implementation;

import com.ubb.events.FollowUpService;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.dto.CreateFollowUpRequest;
import com.ubb.events.dto.FollowUpResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.FollowUpRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.ubb.events.common.errors.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndImplementsCreation() {
        assertTrue(FollowUpServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(FollowUpService.class.isAssignableFrom(FollowUpServiceImpl.class));
    }

    @Test
    void retrievesFollowUpForOrganizationAndEvent() {
        Event event = new Event(3L, new Organization(7L, "Org", null, null), 1L, "Event", "Description",
                LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null, 10L,
                EventStatus.PUBLISHED);
        var followUp = new com.ubb.events.domain.FollowUp(9L, event, "Feedback");
        FollowUpRepository followUps = mock(FollowUpRepository.class);
        when(followUps.findByEvent_IdAndEvent_Organization_Id(3L, 7L)).thenReturn(Optional.of(followUp));

        FollowUpResponse response = new FollowUpServiceImpl(mock(EventRepository.class), followUps, Clock.systemUTC())
                .getFollowUp(7L, 3L);

        assertEquals(9L, response.getId());
        assertEquals(3L, response.getEventId());
        assertEquals("Feedback", response.getContent());
        verify(followUps).findByEvent_IdAndEvent_Organization_Id(3L, 7L);
    }

    @Test
    void missingFollowUpIsNotFoundForRequestedOrganizationAndEvent() {
        FollowUpRepository followUps = mock(FollowUpRepository.class);
        when(followUps.findByEvent_IdAndEvent_Organization_Id(3L, 7L)).thenReturn(Optional.empty());
        var service = new FollowUpServiceImpl(mock(EventRepository.class), followUps, Clock.systemUTC());

        assertThrows(com.ubb.events.common.errors.NotFoundException.class, () -> service.getFollowUp(7L, 3L));
    }

    @Test
    void createsFollowUpOnlyForCompletedEvent() {
        EventRepository events = mock(EventRepository.class);
        FollowUpRepository followUps = mock(FollowUpRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-02T12:00:00Z"), ZoneOffset.UTC);
        Event event = new Event(null, new Organization(7L, "Org", null, null), 1L, "Event", "Description",
                LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null, 10L, EventStatus.PUBLISHED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        when(followUps.save(any())).thenAnswer(invocation -> { var f = invocation.getArgument(0, com.ubb.events.domain.FollowUp.class); f.setId(9L); return f; });
        FollowUpResponse response = new FollowUpServiceImpl(events, followUps, clock)
                .createFollowUp(7L, 3L, new CreateFollowUpRequest("Feedback"));
        assertEquals(9L, response.getId());
        verify(followUps).save(any());
    }

    @Test
    void rejectsEventThatHasNotEnded() {
        EventRepository events = mock(EventRepository.class);
        FollowUpRepository followUps = mock(FollowUpRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:30:00Z"), ZoneOffset.UTC);
        Event event = new Event(null, new Organization(7L, "Org", null, null), 1L, "Event", "Description",
                LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null, 10L, EventStatus.PUBLISHED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        assertThrows(ConflictException.class, () -> new FollowUpServiceImpl(events, followUps, clock)
                .createFollowUp(7L, 3L, new CreateFollowUpRequest("Feedback")));
    }

    @Test
    void rejectsCanceledEventAndInvalidContent() {
        EventRepository events = mock(EventRepository.class);
        FollowUpRepository followUps = mock(FollowUpRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-02T12:00:00Z"), ZoneOffset.UTC);
        Event event = new Event(null, new Organization(7L, "Org", null, null), 1L, "Event", "Description",
                LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null, 10L, EventStatus.CANCELED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        var service = new FollowUpServiceImpl(events, followUps, clock);
        assertThrows(ConflictException.class, () -> service.createFollowUp(7L, 3L, new CreateFollowUpRequest("Feedback")));
        event.setStatus(EventStatus.PUBLISHED);
        assertThrows(com.ubb.events.common.errors.BusinessRuleException.class,
                () -> service.createFollowUp(7L, 3L, new CreateFollowUpRequest(" ")));
    }

    @Test
    void rejectsMissingEndTime() {
        EventRepository events = mock(EventRepository.class);
        FollowUpRepository followUps = mock(FollowUpRepository.class);
        Event event = new Event(null, new Organization(7L, "Org", null, null), 1L, "Event", "Description",
                LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), null, null, 10L, EventStatus.PUBLISHED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        assertThrows(ConflictException.class, () -> new FollowUpServiceImpl(events, followUps, Clock.systemUTC())
                .createFollowUp(7L, 3L, new CreateFollowUpRequest("Feedback")));
    }

    @Test
    void rejectsNullContent() {
        var service = new FollowUpServiceImpl(mock(EventRepository.class), mock(FollowUpRepository.class), Clock.systemUTC());
        assertThrows(com.ubb.events.common.errors.BusinessRuleException.class,
                () -> service.createFollowUp(7L, 3L, new CreateFollowUpRequest(null)));
    }

    @Test
    void rejectsUnknownOrWrongOrganizationEvent() {
        EventRepository events = mock(EventRepository.class);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.empty());
        var service = new FollowUpServiceImpl(events, mock(FollowUpRepository.class), Clock.systemUTC());
        assertThrows(com.ubb.events.common.errors.NotFoundException.class,
                () -> service.createFollowUp(7L, 3L, new CreateFollowUpRequest("Feedback")));
    }

    @Test
    void rejectsEndTimeExactlyAtCurrentTime() {
        EventRepository events = mock(EventRepository.class);
        FollowUpRepository followUps = mock(FollowUpRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-02T12:00:00Z"), ZoneOffset.UTC);
        Event event = new Event(null, new Organization(7L, "Org", null, null), 1L, "Event", "Description",
                LocalDate.of(2026, 1, 2), LocalTime.of(12, 0), LocalTime.of(12, 0), null, 10L, EventStatus.PUBLISHED);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        assertThrows(ConflictException.class, () -> new FollowUpServiceImpl(events, followUps, clock)
                .createFollowUp(7L, 3L, new CreateFollowUpRequest("Feedback")));
    }
}
