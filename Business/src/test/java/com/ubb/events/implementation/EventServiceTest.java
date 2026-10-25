package com.ubb.events.implementation;

import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventDiscoveryFilter;
import com.ubb.events.dto.EventOrganizationSummaryResponse;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.dto.EventSummaryResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.EventSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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

    // "Now" for the time-based rules (publish, upcoming/completed order): 1 November 2026, 10:00 in Cluj (UTC+2 in November).
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

    private static final EventDiscoveryFilter NO_FILTER = new EventDiscoveryFilter(null, null, null, null);
    private static final Sort CHRONOLOGICAL = Sort.by("date", "startTime");

    @Test
    void discoversPublishedEventsAsSummariesSortedChronologically() {
        Organization math = new Organization(8L, "Math club", null, null);
        Event hackathon = new Event(1L, organization, 3L, "Hackathon", "Coding night",
                DATE, START, LocalTime.of(22, 0), "Room 1", 50L, EventStatus.PUBLISHED);
        Event quiz = new Event(2L, math, 4L, "Quiz", "Math quiz",
                DATE.plusDays(1), START, null, null, null, EventStatus.PUBLISHED);
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of(hackathon, quiz));

        List<EventSummaryResponse> result = service.discoverEvents(NO_FILTER);

        assertEquals(List.of(
                new EventSummaryResponse(1L, "Hackathon", DATE, START, LocalTime.of(22, 0), "Room 1", EventStatus.PUBLISHED,
                        new EventOrganizationSummaryResponse(7L, "Students")),
                new EventSummaryResponse(2L, "Quiz", DATE.plusDays(1), START, null, null, EventStatus.PUBLISHED,
                        new EventOrganizationSummaryResponse(8L, "Math club"))), result);
        verify(events).findAll(any(Specification.class), eq(CHRONOLOGICAL));
    }

    @Test
    void discoverReturnsEmptyListWhenNothingMatches() {
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of());

        assertTrue(service.discoverEvents(new EventDiscoveryFilter("nothing", 7L, DATE, DATE)).isEmpty());
        verify(events).findAll(any(Specification.class), eq(CHRONOLOGICAL));
    }

    @Test
    void rejectsFromAfterTo() {
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> service.discoverEvents(new EventDiscoveryFilter(null, null, DATE.plusDays(1), DATE)));

        assertEquals("Invalid date range", exception.getMessage());
        assertEquals(List.of("from"), fields(exception));
        verifyNoInteractions(events);
    }

    @Test
    void acceptsSingleDayAndOpenEndedRanges() {
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of());

        assertTrue(service.discoverEvents(new EventDiscoveryFilter(null, null, DATE, DATE)).isEmpty());
        assertTrue(service.discoverEvents(new EventDiscoveryFilter(null, null, DATE, null)).isEmpty());
        assertTrue(service.discoverEvents(new EventDiscoveryFilter(null, null, null, DATE)).isEmpty());
        verify(events, times(3)).findAll(any(Specification.class), eq(CHRONOLOGICAL));
    }

    @Test
    void ordersUpcomingFirstThenCompletedMostRecentFirst() {
        // The repository returns the chronological order (date, startTime), as the database query does.
        Event oldest = published(1L, LocalDate.of(2026, 10, 1), LocalTime.of(18, 0), LocalTime.of(20, 0));
        Event lastMonth = published(2L, LocalDate.of(2026, 10, 20), LocalTime.of(18, 0), LocalTime.of(20, 0));
        Event endedThisMorning = published(3L, TODAY, LocalTime.of(8, 0), LocalTime.of(9, 0));
        Event runningNow = published(4L, TODAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        Event laterToday = published(5L, TODAY, LocalTime.of(18, 0), LocalTime.of(20, 0));
        Event nextMonth = published(6L, LocalDate.of(2026, 12, 1), LocalTime.of(18, 0), LocalTime.of(20, 0));
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL)))
                .thenReturn(List.of(oldest, lastMonth, endedThisMorning, runningNow, laterToday, nextMonth));

        assertEquals(List.of(4L, 5L, 6L, 3L, 2L, 1L), ids(service.discoverEvents(NO_FILTER)));
    }

    @Test
    void usesStartTimeWhenEndTimeIsMissing() {
        Event startedEarlier = published(1L, TODAY, LocalTime.of(9, 0), null);
        Event startsLater = published(2L, TODAY, LocalTime.of(11, 0), null);
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of(startedEarlier, startsLater));

        assertEquals(List.of(2L, 1L), ids(service.discoverEvents(NO_FILTER)));
    }

    @Test
    void eventEndingExactlyNowIsCompleted() {
        Event endsNow = published(1L, TODAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
        Event endsInOneMinute = published(2L, TODAY, LocalTime.of(9, 30), LocalTime.of(10, 1));
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of(endsNow, endsInOneMinute));

        assertEquals(List.of(2L, 1L), ids(service.discoverEvents(NO_FILTER)));
    }

    private Event published(Long id, LocalDate date, LocalTime start, LocalTime end) {
        return new Event(id, organization, 3L, "Event " + id, "Description", date, start, end, "Room", 10L, EventStatus.PUBLISHED);
    }

    private static List<Long> ids(List<EventSummaryResponse> summaries) {
        return summaries.stream().map(EventSummaryResponse::getId).toList();
    }

    @Test
    void combinesPublishedWithEveryGivenCriterion() {
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of());

        try (MockedStatic<EventSpecifications> specifications = mockStatic(EventSpecifications.class, CALLS_REAL_METHODS)) {
            service.discoverEvents(new EventDiscoveryFilter("hack", 7L, DATE, DATE.plusDays(1)));

            specifications.verify(EventSpecifications::isPublished);
            specifications.verify(() -> EventSpecifications.matchesSearch("hack"));
            specifications.verify(() -> EventSpecifications.inOrganization(7L));
            specifications.verify(() -> EventSpecifications.onOrAfter(DATE));
            specifications.verify(() -> EventSpecifications.onOrBefore(DATE.plusDays(1)));
        }
    }

    @Test
    void emptyFilterOnlyRequiresPublishedAndBlankSearchIsIgnored() {
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of());

        try (MockedStatic<EventSpecifications> specifications = mockStatic(EventSpecifications.class, CALLS_REAL_METHODS)) {
            service.discoverEvents(NO_FILTER);
            service.discoverEvents(new EventDiscoveryFilter("   ", null, null, null));

            specifications.verify(EventSpecifications::isPublished, times(2));
            specifications.verify(() -> EventSpecifications.matchesSearch(anyString()), never());
            specifications.verify(() -> EventSpecifications.inOrganization(any()), never());
            specifications.verify(() -> EventSpecifications.onOrAfter(any()), never());
            specifications.verify(() -> EventSpecifications.onOrBefore(any()), never());
        }
    }

    @Test
    void openEndedRangeOnlyAddsTheGivenBound() {
        when(events.findAll(any(Specification.class), eq(CHRONOLOGICAL))).thenReturn(List.of());

        try (MockedStatic<EventSpecifications> specifications = mockStatic(EventSpecifications.class, CALLS_REAL_METHODS)) {
            service.discoverEvents(new EventDiscoveryFilter(null, null, DATE, null));

            specifications.verify(() -> EventSpecifications.onOrAfter(DATE));
            specifications.verify(() -> EventSpecifications.onOrBefore(any()), never());
        }
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
    void publishOnlyChecksTheStartIsInTheFuture() {
        // Field validation (title, description, end time, location, capacity) belongs to create (T23)
        // and edit (T26); publishing only rejects events that do not start in the future.
        Event draft = completeDraft();
        draft.setTitle(" ");
        draft.setDescription("");
        draft.setEndTime(START.minusHours(1));
        draft.setLocation("  ");
        draft.setCapacity(0L);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(draft));
        when(events.save(draft)).thenReturn(draft);

        assertEquals(EventStatus.PUBLISHED, service.publishEvent(7L, 1L).getStatus());
        verify(events).save(draft);
    }

    @Test
    void pastStartIsTheOnlyPublishValidationError() {
        Event pastAndIncomplete = completeDraft();
        pastAndIncomplete.setDate(TODAY);
        pastAndIncomplete.setStartTime(LocalTime.of(9, 0));
        pastAndIncomplete.setEndTime(null);
        pastAndIncomplete.setLocation(null);
        pastAndIncomplete.setCapacity(null);
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(pastAndIncomplete));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.publishEvent(7L, 1L));

        assertEquals("Event is not ready to be published", exception.getMessage());
        assertEquals(List.of("date"), fields(exception));
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
