package com.ubb.events.implementation;

import com.ubb.events.EventTaskService;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventTask;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.User;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.dto.MyTaskEventResponse;
import com.ubb.events.dto.MyTaskOrganizationResponse;
import com.ubb.events.dto.MyTaskResponse;
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

    @Override
    public List<MyTaskResponse> listMyTasks(Long userId) {
        return tasks.findAllByAssignedUserIdOrderByEventDateAscEventStartTimeAscIdAsc(userId).stream()
                .map(EventTaskServiceImpl::toMyTaskResponse)
                .toList();
    }

    private static MyTaskResponse toMyTaskResponse(EventTask task) {
        Event event = task.getEvent();
        Organization organization = event.getOrganization();
        return new MyTaskResponse(
                task.getId(),
                task.getDescription(),
                new MyTaskEventResponse(event.getId(), event.getTitle(), event.getDate(), event.getStartTime(), event.getStatus()),
                new MyTaskOrganizationResponse(organization.getId(), organization.getName()));
    }

    private static EventTaskResponse toResponse(EventTask task) {
        User assignee = task.getAssignedUser();
        return new EventTaskResponse(
                task.getId(),
                task.getDescription(),
                assignee == null ? null : new TaskAssigneeResponse(assignee.getId(), assignee.getFirstName(), assignee.getLastName()));
    }
}
