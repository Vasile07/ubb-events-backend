package com.ubb.events.implementation;

import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.EventTask;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.User;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.dto.TaskAssigneeResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.EventTaskRepository;
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
class EventTaskServiceTest {
    @Mock EventTaskRepository tasks;
    @Mock EventRepository events;
    @InjectMocks EventTaskServiceImpl service;

    private final Organization organization = new Organization(7L, "Students", null, null);
    private final Event event = new Event(1L, organization, 3L, "Hackathon", "Coding night",
            LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), null, null, null, EventStatus.DRAFT);

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
