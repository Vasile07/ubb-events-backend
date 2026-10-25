package com.ubb.events.implementation;

import com.ubb.events.EventTaskService;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.EventTask;
import com.ubb.events.domain.User;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.dto.TaskAssigneeResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.EventTaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventTaskServiceImpl implements EventTaskService {
    private final EventTaskRepository tasks;
    private final EventRepository events;

    public EventTaskServiceImpl(EventTaskRepository tasks, EventRepository events) {
        this.tasks = tasks;
        this.events = events;
    }

    @Override
    public List<EventTaskResponse> listEventTasks(Long organizationId, Long eventId) {
        if (events.findByIdAndOrganizationId(eventId, organizationId).isEmpty()) {
            throw new NotFoundException("Event not found");
        }
        return tasks.findAllByEventIdOrderByIdAsc(eventId).stream()
                .map(EventTaskServiceImpl::toResponse)
                .toList();
    }

    private static EventTaskResponse toResponse(EventTask task) {
        User assignee = task.getAssignedUser();
        return new EventTaskResponse(
                task.getId(),
                task.getDescription(),
                assignee == null ? null : new TaskAssigneeResponse(assignee.getId(), assignee.getFirstName(), assignee.getLastName()));
    }
}
