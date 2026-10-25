package com.ubb.events.implementation;

import com.ubb.events.EventService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventOrganizationSummaryResponse;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.dto.EventSummaryResponse;
import com.ubb.events.persistence.EventRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public List<EventSummaryResponse> discoverEvents() {
        return events.findAllByStatusOrderByDateAscStartTimeAsc(EventStatus.PUBLISHED).stream()
                .map(EventServiceImpl::toSummaryResponse)
                .toList();
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
