package com.ubb.events.implementation;

import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.persistence.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);
    private static final LocalTime START = LocalTime.of(18, 0);

    // "Now" for the publish rules: 1 November 2026, 10:00 in Cluj (UTC+2 in November).
    private static final ZoneId CLUJ = ZoneId.of("Europe/Bucharest");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-01T08:00:00Z"), CLUJ);
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);

    @Mock EventRepository events;
    EventServiceImpl service;

    private final Organization organization = new Organization(7L, "Students", null, null);

    @BeforeEach
    void setUp() {
        service = new EventServiceImpl(events, CLOCK);
    }

    private Event completeDraft() {
        return new Event(1L, organization, 3L, "Hackathon", "Coding night",
                DATE, START, LocalTime.of(22, 0), "Room 1", 50L, EventStatus.DRAFT);
    }

    @Test
    void publishesCompleteDraftAndReturnsUpdatedEvent() {
        Event draft = completeDraft();
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(draft));
        when(events.save(draft)).thenReturn(draft);

        EventResponse result = service.publishEvent(7L, 1L);

        assertEquals(new EventResponse(1L, 7L, 3L, "Hackathon", "Coding night",
                DATE, START, LocalTime.of(22, 0), "Room 1", 50L, EventStatus.PUBLISHED), result);
        assertEquals(EventStatus.PUBLISHED, draft.getStatus());
        verify(events).save(draft);
    }

    @Test
    void rejectsIncompleteDraftListingEveryFieldToCorrect() {
        Event draft = completeDraft();
        draft.setEndTime(null);
        draft.setLocation("  ");
        draft.setCapacity(null);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(draft));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L));

        assertEquals("Event is not ready to be published", exception.getMessage());
        assertEquals(List.of("endTime", "location", "capacity"), fields(exception));
        assertTrue(exception.getErrors().stream().allMatch(error -> error.getMessage() != null && !error.getMessage().isBlank()));
        assertEquals(EventStatus.DRAFT, draft.getStatus());
        verify(events, never()).save(any());
    }

    @Test
    void rejectsEndTimeNotAfterStartTime() {
        Event equal = completeDraft();
        equal.setEndTime(START);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(equal));
        assertEquals(List.of("endTime"), fields(assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L))));

        Event before = completeDraft();
        before.setEndTime(START.minusHours(1));
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(before));
        assertEquals(List.of("endTime"), fields(assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L))));
        verify(events, never()).save(any());
    }

    @Test
    void rejectsNonPositiveCapacity() {
        Event draft = completeDraft();
        draft.setCapacity(0L);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(draft));

        assertEquals(List.of("capacity"), fields(assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L))));
        verify(events, never()).save(any());
    }

    @Test
    void rejectsBlankTitleOrDescription() {
        Event draft = completeDraft();
        draft.setTitle(" ");
        draft.setDescription("");
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(draft));

        assertEquals(List.of("title", "description"),
                fields(assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L))));
        verify(events, never()).save(any());
    }

    @Test
    void rejectsPublishingEventThatStartsInThePast() {
        Event earlierToday = completeDraft();
        earlierToday.setDate(TODAY);
        earlierToday.setStartTime(LocalTime.of(9, 0));
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(earlierToday));
        assertEquals(List.of("date"), fields(assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L))));

        Event startingNow = completeDraft();
        startingNow.setDate(TODAY);
        startingNow.setStartTime(LocalTime.of(10, 0));
        startingNow.setEndTime(LocalTime.of(12, 0));
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(startingNow));
        assertEquals(List.of("date"), fields(assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L))));
        verify(events, never()).save(any());
    }

    @Test
    void acceptsEventStartingLaterToday() {
        Event laterToday = completeDraft();
        laterToday.setDate(TODAY);
        laterToday.setStartTime(LocalTime.of(10, 1));
        laterToday.setEndTime(LocalTime.of(12, 0));
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(laterToday));
        when(events.save(laterToday)).thenReturn(laterToday);

        assertEquals(EventStatus.PUBLISHED, service.publishEvent(7L, 1L).getStatus());
    }

    @Test
    void rejectsAlreadyPublishedEvent() {
        Event published = completeDraft();
        published.setStatus(EventStatus.PUBLISHED);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(published));

        ConflictException exception = assertThrows(ConflictException.class, () -> service.publishEvent(7L, 1L));
        assertEquals("Event is already published", exception.getMessage());
        verify(events, never()).save(any());
    }

    @Test
    void rejectsCanceledEvent() {
        Event canceled = completeDraft();
        canceled.setStatus(EventStatus.CANCELED);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(canceled));

        ConflictException exception = assertThrows(ConflictException.class, () -> service.publishEvent(7L, 1L));
        assertEquals("A canceled event cannot be published", exception.getMessage());
        verify(events, never()).save(any());
    }

    @Test
    void throwsNotFoundWhenPublishingEventOfAnotherOrganization() {
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.publishEvent(7L, 1L));
        assertEquals("Event not found", exception.getMessage());
        verify(events, never()).save(any());
    }

    private static List<String> fields(BusinessRuleException exception) {
        return exception.getErrors().stream().map(ErrorDetail::getField).toList();
    }

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
