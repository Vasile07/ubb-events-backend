package com.ubb.events.implementation;

import com.ubb.events.EventService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.EventTask;
import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventDiscoveryFilter;
import com.ubb.events.dto.EventOrganizationSummaryResponse;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.dto.EventSummaryResponse;
import com.ubb.events.dto.CreateEventRequest;
import com.ubb.events.dto.EventTaskRequest;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.EventTaskRepository;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.OrganizationRepository;
import com.ubb.events.persistence.EventSpecifications;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ubb.events.domain.User;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository events;
    private final Clock clock;
    private final EventTaskRepository tasks;
    private final MembershipRepository memberships;
    private final OrganizationRepository organizations;

    public EventServiceImpl(EventRepository events, Clock clock, EventTaskRepository tasks,
            MembershipRepository memberships, OrganizationRepository organizations) {
        this.events = events;
        this.clock = clock;
        this.tasks = tasks;
        this.memberships = memberships;
        this.organizations = organizations;
    }

    @Override
    @Transactional
    public EventResponse createEvent(Long organizationId, CreateEventRequest request) {
        List<ErrorDetail> errors = validateEventDetails(request);
        if (!errors.isEmpty()) {
            throw new BusinessRuleException("Event details are invalid", errors);
        }

        Organization organization = organizations.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
        List<EventTaskRequest> taskRequests = request.getTasks() == null ? List.of() : request.getTasks();
        List<Membership> assignees = new ArrayList<>();
        for (int i = 0; i < taskRequests.size(); i++) {
            EventTaskRequest taskRequest = taskRequests.get(i);
            if (taskRequest == null || taskRequest.getDescription() == null || taskRequest.getDescription().isBlank()) {
                errors.add(new ErrorDetail("tasks[" + i + "].description", "Task description is required"));
                continue;
            }
            Long assigneeId = taskRequest.getAssigneeUserId();
            if (assigneeId == null) {
                assignees.add(null);
                continue;
            }
            Membership membership = memberships.findByUserIdAndOrganizationId(assigneeId, organizationId).orElse(null);
            if (membership == null || membership.getStatus() != MembershipStatus.ACTIVE) {
                errors.add(new ErrorDetail("tasks[" + i + "].assigneeUserId",
                        "Assignee must be an active member of the organization"));
                assignees.add(null);
            } else {
                assignees.add(membership);
            }
        }
        if (!errors.isEmpty()) {
            throw new BusinessRuleException("Event details are invalid", errors);
        }

        Event event = new Event(null, organization, request.getCategoryId(), request.getTitle(), request.getDescription(),
                request.getDate(), request.getStartTime(), request.getEndTime(), request.getLocation(),
                request.getCapacity(), EventStatus.DRAFT);
        Event savedEvent = events.save(event);
        List<EventTask> eventTasks = new ArrayList<>();
        for (int i = 0; i < taskRequests.size(); i++) {
            Membership membership = assignees.get(i);
            User assignee = membership == null ? null : membership.getUser();
            eventTasks.add(new EventTask(null, taskRequests.get(i).getDescription(), assignee, savedEvent));
        }
        if (!eventTasks.isEmpty()) {
            tasks.saveAll(eventTasks);
        }
        return toResponse(savedEvent);
    }

    private List<ErrorDetail> validateEventDetails(CreateEventRequest request) {
        List<ErrorDetail> errors = new ArrayList<>();
        if (request == null) {
            errors.add(new ErrorDetail("event", "Event details are required"));
            return errors;
        }
        if (request.getCategoryId() == null) {
            errors.add(new ErrorDetail("categoryId", "Category is required"));
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            errors.add(new ErrorDetail("title", "Title is required"));
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            errors.add(new ErrorDetail("description", "Description is required"));
        }
        if (request.getDate() == null) {
            errors.add(new ErrorDetail("date", "Date is required"));
        }
        if (request.getStartTime() == null) {
            errors.add(new ErrorDetail("startTime", "Start time is required"));
        }
        if (request.getDate() != null && request.getStartTime() != null
                && !LocalDateTime.of(request.getDate(), request.getStartTime()).isAfter(LocalDateTime.now(clock))) {
            errors.add(new ErrorDetail("date", "Event must start in the future"));
        }
        if (request.getEndTime() == null) {
            errors.add(new ErrorDetail("endTime", "End time is required"));
        } else if (request.getStartTime() != null && !request.getEndTime().isAfter(request.getStartTime())) {
            errors.add(new ErrorDetail("endTime", "End time must be after start time"));
        }
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            errors.add(new ErrorDetail("location", "Location is required"));
        }
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            errors.add(new ErrorDetail("capacity", "Capacity must be greater than zero"));
        }
        return errors;
    }

    @Override
    public List<EventResponse> listOrganizationEvents(Long organizationId) {
        return events.findAllByOrganizationIdOrderByDateAscStartTimeAsc(organizationId).stream()
                .map(EventServiceImpl::toResponse)
                .toList();
    }

    @Override
    public EventResponse getOrganizationEvent(Long organizationId, Long eventId) {
        return events.findByIdAndOrganizationId(eventId, organizationId)
                .map(EventServiceImpl::toResponse)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    @Override
    @Transactional
    public EventResponse publishEvent(Long organizationId, Long eventId) {
        Event event = events.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new ConflictException("Event is already published");
        }
        if (event.getStatus() == EventStatus.CANCELED) {
            throw new ConflictException("A canceled event cannot be published");
        }

        List<ErrorDetail> errors = validateForPublication(event);
        if (!errors.isEmpty()) {
            throw new BusinessRuleException("Event is not ready to be published", errors);
        }

        event.setStatus(EventStatus.PUBLISHED);
        return toResponse(events.save(event));
    }

    @Override
    public List<EventSummaryResponse> discoverEvents(EventDiscoveryFilter filter) {
        if (filter.getFrom() != null && filter.getTo() != null && filter.getFrom().isAfter(filter.getTo())) {
            throw new BusinessRuleException("Invalid date range",
                    List.of(new ErrorDetail("from", "From date must not be after the to date")));
        }
        LocalDateTime now = LocalDateTime.now(clock);
        List<Event> chronological = events.findAll(discoverySpecification(filter), Sort.by("date", "startTime"));
        List<Event> upcoming = new ArrayList<>();
        List<Event> completed = new ArrayList<>();
        for (Event event : chronological) {
            (hasEnded(event, now) ? completed : upcoming).add(event);
        }
        Collections.reverse(completed);

        List<EventSummaryResponse> result = new ArrayList<>();
        upcoming.forEach(event -> result.add(toSummaryResponse(event)));
        completed.forEach(event -> result.add(toSummaryResponse(event)));
        return result;
    }

    // An event has ended when its end (or its start, if it has no end time) is not after now.
    private static boolean hasEnded(Event event, LocalDateTime now) {
        LocalTime end = event.getEndTime() != null ? event.getEndTime() : event.getStartTime();
        return !LocalDateTime.of(event.getDate(), end).isAfter(now);
    }

    private static Specification<Event> discoverySpecification(EventDiscoveryFilter filter) {
        Specification<Event> specification = EventSpecifications.isPublished();
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            specification = specification.and(EventSpecifications.matchesSearch(filter.getSearch()));
        }
        if (filter.getOrganizationId() != null) {
            specification = specification.and(EventSpecifications.inOrganization(filter.getOrganizationId()));
        }
        if (filter.getFrom() != null) {
            specification = specification.and(EventSpecifications.onOrAfter(filter.getFrom()));
        }
        if (filter.getTo() != null) {
            specification = specification.and(EventSpecifications.onOrBefore(filter.getTo()));
        }
        return specification;
    }

    private static EventSummaryResponse toSummaryResponse(Event event) {
        return new EventSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getStatus(),
                new EventOrganizationSummaryResponse(event.getOrganization().getId(), event.getOrganization().getName()));
    }

    // Field validation (title, description, end time, location, capacity) is done when the event is
    // created (T23) and edited (T26); publishing only rejects an event that does not start in the future.
    private List<ErrorDetail> validateForPublication(Event event) {
        List<ErrorDetail> errors = new ArrayList<>();
        if (event.getDate() != null && event.getStartTime() != null
                && !LocalDateTime.of(event.getDate(), event.getStartTime()).isAfter(LocalDateTime.now(clock))) {
            errors.add(new ErrorDetail("date", "Event must start in the future"));
        }
        return errors;
    }

    private static EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getOrganization().getId(),
                event.getCategoryId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getCapacity(),
                event.getStatus());
    }
}
