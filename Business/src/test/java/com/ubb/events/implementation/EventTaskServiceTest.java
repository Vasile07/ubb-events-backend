package com.ubb.events.implementation;

import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.EventTask;
import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.User;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventTaskRequest;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.dto.MyTaskEventResponse;
import com.ubb.events.dto.MyTaskOrganizationResponse;
import com.ubb.events.dto.MyTaskResponse;
import com.ubb.events.dto.TaskAssigneeResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.EventTaskRepository;
import com.ubb.events.persistence.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventTaskServiceTest {
    // "Now": 1 November 2026, 10:00 in Cluj (UTC+2 in November).
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-01T08:00:00Z"), ZoneId.of("Europe/Bucharest"));

    @Mock EventTaskRepository tasks;
    @Mock EventRepository events;
    @Mock MembershipRepository memberships;
    EventTaskServiceImpl service;

    private final Organization organization = new Organization(7L, "Students", null, null);
    private final Event event = new Event(1L, organization, 3L, "Hackathon", "Coding night",
            LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), null, null, null, EventStatus.DRAFT);
    private final User ana = new User(42L, "Ana", "Student", "ana@example.com", "secret");
    private final User bob = new User(43L, "Bob", "Member", "bob@example.com", "secret");

    @BeforeEach
    void setUp() {
        service = new EventTaskServiceImpl(tasks, events, memberships, CLOCK);
    }

    private void eventExists(Event existing) {
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(existing));
    }

    private void activeMember(User user) {
        when(memberships.findByUserIdAndOrganizationId(user.getId(), 7L)).thenReturn(Optional.of(
                new Membership(100L, user, organization, MembershipRole.MEMBER, MembershipStatus.ACTIVE)));
    }

    private void savedTasksGetId(long id) {
        when(tasks.save(any(EventTask.class))).thenAnswer(invocation -> {
            EventTask task = invocation.getArgument(0);
            if (task.getId() == null) {
                task.setId(id);
            }
            return task;
        });
    }

    private static List<String> fields(BusinessRuleException exception) {
        return exception.getErrors().stream().map(ErrorDetail::getField).toList();
    }

    @Test
    void addsTaskWithActiveMemberAssignee() {
        eventExists(event);
        activeMember(ana);
        savedTasksGetId(10L);

        EventTaskResponse result = service.addTask(7L, 1L, new EventTaskRequest("Book the room", 42L));

        assertEquals(new EventTaskResponse(10L, "Book the room", new TaskAssigneeResponse(42L, "Ana", "Student")), result);
        verify(tasks).save(argThat(task -> task.getEvent() == event && task.getAssignedUser() == ana
                && "Book the room".equals(task.getDescription())));
    }

    @Test
    void addsUnassignedTask() {
        eventExists(event);
        savedTasksGetId(11L);

        assertEquals(new EventTaskResponse(11L, "Print posters", null),
                service.addTask(7L, 1L, new EventTaskRequest("Print posters", null)));
        verifyNoInteractions(memberships);
    }

    @Test
    void updatesDescriptionAndReassigns() {
        EventTask existing = new EventTask(10L, "Book room", bob, event);
        eventExists(event);
        when(tasks.findByIdAndEventId(10L, 1L)).thenReturn(Optional.of(existing));
        activeMember(ana);
        savedTasksGetId(10L);

        EventTaskResponse result = service.updateTask(7L, 1L, 10L, new EventTaskRequest("Book the big room", 42L));

        assertEquals(new EventTaskResponse(10L, "Book the big room", new TaskAssigneeResponse(42L, "Ana", "Student")), result);
        assertEquals(ana, existing.getAssignedUser());
        verify(tasks).save(existing);
    }

    @Test
    void unassignsWithNullAssignee() {
        EventTask existing = new EventTask(10L, "Book room", bob, event);
        eventExists(event);
        when(tasks.findByIdAndEventId(10L, 1L)).thenReturn(Optional.of(existing));
        savedTasksGetId(10L);

        assertEquals(new EventTaskResponse(10L, "Book room", null),
                service.updateTask(7L, 1L, 10L, new EventTaskRequest("Book room", null)));
        assertNull(existing.getAssignedUser());
    }

    @Test
    void removesTask() {
        EventTask existing = new EventTask(10L, "Book room", bob, event);
        eventExists(event);
        when(tasks.findByIdAndEventId(10L, 1L)).thenReturn(Optional.of(existing));

        service.removeTask(7L, 1L, 10L);

        verify(tasks).delete(existing);
    }

    @Test
    void rejectsNonMemberOrInactiveAssignee() {
        eventExists(event);
        when(memberships.findByUserIdAndOrganizationId(99L, 7L)).thenReturn(Optional.empty());
        BusinessRuleException nonMember = assertThrows(BusinessRuleException.class,
                () -> service.addTask(7L, 1L, new EventTaskRequest("Book the room", 99L)));
        assertEquals(List.of("assigneeUserId"), fields(nonMember));
        assertEquals("Assignee must be an active member of the organization", nonMember.getErrors().get(0).getMessage());

        when(memberships.findByUserIdAndOrganizationId(43L, 7L)).thenReturn(Optional.of(
                new Membership(101L, bob, organization, MembershipRole.MEMBER, MembershipStatus.PENDING)));
        assertEquals(List.of("assigneeUserId"), fields(assertThrows(BusinessRuleException.class,
                () -> service.addTask(7L, 1L, new EventTaskRequest("Book the room", 43L)))));
        verify(tasks, never()).save(any());
    }

    @Test
    void rejectsBlankDescription() {
        eventExists(event);
        EventTask existing = new EventTask(10L, "Book room", null, event);
        when(tasks.findByIdAndEventId(10L, 1L)).thenReturn(Optional.of(existing));

        assertEquals(List.of("description"), fields(assertThrows(BusinessRuleException.class,
                () -> service.addTask(7L, 1L, new EventTaskRequest("  ", null)))));
        assertEquals(List.of("description"), fields(assertThrows(BusinessRuleException.class,
                () -> service.updateTask(7L, 1L, 10L, new EventTaskRequest(null, null)))));
        assertEquals("Book room", existing.getDescription());
        verify(tasks, never()).save(any());
    }

    @Test
    void rejectsTaskOfAnotherEvent() {
        eventExists(event);
        when(tasks.findByIdAndEventId(10L, 1L)).thenReturn(Optional.empty());

        assertEquals("Task not found", assertThrows(NotFoundException.class,
                () -> service.updateTask(7L, 1L, 10L, new EventTaskRequest("Book room", null))).getMessage());
        assertEquals("Task not found", assertThrows(NotFoundException.class,
                () -> service.removeTask(7L, 1L, 10L)).getMessage());
        verify(tasks, never()).save(any());
        verify(tasks, never()).delete(any());
    }

    @Test
    void rejectsEventOfAnotherOrganization() {
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.empty());

        assertEquals("Event not found", assertThrows(NotFoundException.class,
                () -> service.addTask(7L, 1L, new EventTaskRequest("Book room", null))).getMessage());
        assertEquals("Event not found", assertThrows(NotFoundException.class,
                () -> service.updateTask(7L, 1L, 10L, new EventTaskRequest("Book room", null))).getMessage());
        assertEquals("Event not found", assertThrows(NotFoundException.class,
                () -> service.removeTask(7L, 1L, 10L)).getMessage());
        verifyNoInteractions(tasks);
    }

    @Test
    void rejectsTaskChangesOnCanceledOrEndedEvent() {
        Event canceled = new Event(1L, organization, 3L, "Party", "Canceled", LocalDate.of(2026, 11, 20),
                LocalTime.of(18, 0), LocalTime.of(20, 0), "Room", 10L, EventStatus.CANCELED);
        eventExists(canceled);
        assertEquals("Tasks of a canceled event cannot be changed", assertThrows(ConflictException.class,
                () -> service.addTask(7L, 1L, new EventTaskRequest("Book room", null))).getMessage());
        assertThrows(ConflictException.class, () -> service.updateTask(7L, 1L, 10L, new EventTaskRequest("Book room", null)));
        assertThrows(ConflictException.class, () -> service.removeTask(7L, 1L, 10L));

        Event ended = new Event(1L, organization, 3L, "Past", "Ended", LocalDate.of(2026, 10, 1),
                LocalTime.of(18, 0), LocalTime.of(20, 0), "Room", 10L, EventStatus.PUBLISHED);
        eventExists(ended);
        assertEquals("Tasks of an ended event cannot be changed", assertThrows(ConflictException.class,
                () -> service.addTask(7L, 1L, new EventTaskRequest("Book room", null))).getMessage());
        assertThrows(ConflictException.class, () -> service.removeTask(7L, 1L, 10L));
        verify(tasks, never()).save(any());
        verify(tasks, never()).delete(any());
    }

    @Test
    void listsMyTasksWithEventAndOrganization() {
        User ana = new User(42L, "Ana", "Student", "ana@example.com", "secret");
        Organization math = new Organization(8L, "Math club", null, null);
        Event canceled = new Event(2L, math, 3L, "Party", "Canceled party",
                LocalDate.of(2026, 12, 1), LocalTime.of(20, 0), null, null, null, EventStatus.CANCELED);
        when(tasks.findAllByAssignedUserIdOrderByEventDateAscEventStartTimeAscIdAsc(42L)).thenReturn(List.of(
                new EventTask(10L, "Book the room", ana, event),
                new EventTask(12L, "Buy snacks", ana, canceled)));

        List<MyTaskResponse> result = service.listMyTasks(42L);

        assertEquals(List.of(
                new MyTaskResponse(10L, "Book the room",
                        new MyTaskEventResponse(1L, "Hackathon", LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), EventStatus.DRAFT),
                        new MyTaskOrganizationResponse(7L, "Students")),
                new MyTaskResponse(12L, "Buy snacks",
                        new MyTaskEventResponse(2L, "Party", LocalDate.of(2026, 12, 1), LocalTime.of(20, 0), EventStatus.CANCELED),
                        new MyTaskOrganizationResponse(8L, "Math club"))), result);
        verify(tasks).findAllByAssignedUserIdOrderByEventDateAscEventStartTimeAscIdAsc(42L);
        verifyNoInteractions(events);
    }

    @Test
    void returnsEmptyListWhenUserHasNoTasks() {
        when(tasks.findAllByAssignedUserIdOrderByEventDateAscEventStartTimeAscIdAsc(42L)).thenReturn(List.of());

        assertTrue(service.listMyTasks(42L).isEmpty());
        verify(tasks).findAllByAssignedUserIdOrderByEventDateAscEventStartTimeAscIdAsc(42L);
    }

    @Test
    void listsTasksOfEventOfOrganizationWithAssignees() {
        User ana = new User(42L, "Ana", "Student", "ana@example.com", "secret");
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(event));
        when(tasks.findAllByEventIdOrderByIdAsc(1L)).thenReturn(List.of(
                new EventTask(10L, "Book the room", ana, event),
                new EventTask(11L, "Print posters", null, event)));

        List<EventTaskResponse> result = service.listEventTasks(7L, 1L);

        assertEquals(List.of(
                new EventTaskResponse(10L, "Book the room", new TaskAssigneeResponse(42L, "Ana", "Student")),
                new EventTaskResponse(11L, "Print posters", null)), result);
        verify(events).findByIdAndOrganizationId(1L, 7L);
        verify(tasks).findAllByEventIdOrderByIdAsc(1L);
    }

    @Test
    void returnsEmptyListForEventWithoutTasks() {
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.of(event));
        when(tasks.findAllByEventIdOrderByIdAsc(1L)).thenReturn(List.of());

        assertTrue(service.listEventTasks(7L, 1L).isEmpty());
        verify(tasks).findAllByEventIdOrderByIdAsc(1L);
    }

    @Test
    void throwsNotFoundWhenEventNotInOrganization() {
        when(events.findByIdAndOrganizationId(1L, 7L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.listEventTasks(7L, 1L));
        assertEquals("Event not found", exception.getMessage());
        verifyNoInteractions(tasks);
    }
}
