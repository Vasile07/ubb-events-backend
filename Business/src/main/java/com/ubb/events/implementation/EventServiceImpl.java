package com.ubb.events.implementation;

import com.ubb.events.EventService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventDiscoveryFilter;
import com.ubb.events.dto.EventOrganizationSummaryResponse;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.dto.EventSummaryResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.EventSpecifications;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository events;
    private final Clock clock;

    public EventServiceImpl(EventRepository events, Clock clock) {
        this.events = events;
        this.clock = clock;
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
        return events.findAll(discoverySpecification(filter), Sort.by("date", "startTime")).stream()
                .map(EventServiceImpl::toSummaryResponse)
                .toList();
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

    private List<ErrorDetail> validateForPublication(Event event) {
        List<ErrorDetail> errors = new ArrayList<>();
        if (isBlank(event.getTitle())) {
            errors.add(new ErrorDetail("title", "Title is required"));
        }
        if (isBlank(event.getDescription())) {
            errors.add(new ErrorDetail("description", "Description is required"));
        }
        if (event.getDate() == null) {
            errors.add(new ErrorDetail("date", "Date is required"));
        }
        if (event.getStartTime() == null) {
            errors.add(new ErrorDetail("startTime", "Start time is required"));
        }
        if (event.getDate() != null && event.getStartTime() != null
                && !LocalDateTime.of(event.getDate(), event.getStartTime()).isAfter(LocalDateTime.now(clock))) {
            errors.add(new ErrorDetail("date", "Event must start in the future"));
        }
        if (event.getEndTime() == null) {
            errors.add(new ErrorDetail("endTime", "End time is required"));
        } else if (event.getStartTime() != null && !event.getEndTime().isAfter(event.getStartTime())) {
            errors.add(new ErrorDetail("endTime", "End time must be after the start time"));
        }
        if (isBlank(event.getLocation())) {
            errors.add(new ErrorDetail("location", "Location is required"));
        }
        if (event.getCapacity() == null) {
            errors.add(new ErrorDetail("capacity", "Capacity is required"));
        } else if (event.getCapacity() <= 0) {
            errors.add(new ErrorDetail("capacity", "Capacity must be a positive number"));
        }
        return errors;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
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
