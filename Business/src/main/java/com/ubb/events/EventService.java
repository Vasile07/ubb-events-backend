package com.ubb.events;

import com.ubb.events.dto.EventDiscoveryFilter;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.dto.EventSummaryResponse;
import com.ubb.events.dto.CreateEventRequest;
import java.util.List;

public interface EventService {
    EventResponse createEvent(Long organizationId, CreateEventRequest request);
    List<EventResponse> listOrganizationEvents(Long organizationId);
    EventResponse getOrganizationEvent(Long organizationId, Long eventId);
    EventResponse publishEvent(Long organizationId, Long eventId);
    List<EventSummaryResponse> discoverEvents(EventDiscoveryFilter filter);
}
