package com.ubb.events.implementation;

import com.ubb.events.EventTaskService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.EventTask;
import com.ubb.events.domain.Membership;
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventTaskServiceImpl implements EventTaskService {
    private final EventTaskRepository tasks;
    private final EventRepository events;
    private final MembershipRepository memberships;
    private final Clock clock;

    public EventTaskServiceImpl(EventTaskRepository tasks, EventRepository events,
            MembershipRepository memberships, Clock clock) {
        this.tasks = tasks;
        this.events = events;
        this.memberships = memberships;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EventTaskResponse addTask(Long organizationId, Long eventId, EventTaskRequest request) {
        Event event = editableEvent(organizationId, eventId);
        User assignee = validatedAssignee(organizationId, request);
        return toResponse(tasks.save(new EventTask(null, request.getDescription(), assignee, event)));
    }

    @Override
    @Transactional
    public EventTaskResponse updateTask(Long organizationId, Long eventId, Long taskId, EventTaskRequest request) {
        editableEvent(organizationId, eventId);
        EventTask task = tasks.findByIdAndEventId(taskId, eventId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        User assignee = validatedAssignee(organizationId, request);
        task.setDescription(request.getDescription());
        task.setAssignedUser(assignee);
        return toResponse(tasks.save(task));
    }

    @Override
    @Transactional
    public void removeTask(Long organizationId, Long eventId, Long taskId) {
        editableEvent(organizationId, eventId);
        EventTask task = tasks.findByIdAndEventId(taskId, eventId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        tasks.delete(task);
    }

    // Tasks can only change while the event is neither canceled nor ended (same "ended" rule as EventServiceImpl).
    private Event editableEvent(Long organizationId, Long eventId) {
        Event event = events.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getStatus() == EventStatus.CANCELED) {
            throw new ConflictException("Tasks of a canceled event cannot be changed");
        }
        LocalTime end = event.getEndTime() != null ? event.getEndTime() : event.getStartTime();
        if (!LocalDateTime.of(event.getDate(), end).isAfter(LocalDateTime.now(clock))) {
            throw new ConflictException("Tasks of an ended event cannot be changed");
        }
        return event;
    }

    // Description is required; an assignee is optional but must be an ACTIVE member of the organization (as in T23).
    private User validatedAssignee(Long organizationId, EventTaskRequest request) {
        List<ErrorDetail> errors = new ArrayList<>();
        if (request == null || request.getDescription() == null || request.getDescription().isBlank()) {
            errors.add(new ErrorDetail("description", "Task description is required"));
        }
        User assignee = null;
        Long assigneeId = request == null ? null : request.getAssigneeUserId();
        if (assigneeId != null) {
            Membership membership = memberships.findByUserIdAndOrganizationId(assigneeId, organizationId).orElse(null);
            if (membership == null || membership.getStatus() != MembershipStatus.ACTIVE) {
                errors.add(new ErrorDetail("assigneeUserId", "Assignee must be an active member of the organization"));
            } else {
                assignee = membership.getUser();
            }
        }
        if (!errors.isEmpty()) {
            throw new BusinessRuleException("Task details are invalid", errors);
        }
        return assignee;
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
