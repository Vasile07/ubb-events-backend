package com.ubb.events.implementation;

import com.ubb.events.EventService;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.persistence.EventRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository events;

    public EventServiceImpl(EventRepository events) {
        this.events = events;
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
