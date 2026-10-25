package com.ubb.events;

import com.ubb.events.dto.EventResponse;
import com.ubb.events.dto.EventSummaryResponse;
import java.util.List;

public interface EventService {
    List<EventResponse> listOrganizationEvents(Long organizationId);
    EventResponse getOrganizationEvent(Long organizationId, Long eventId);
    EventResponse publishEvent(Long organizationId, Long eventId);
    List<EventSummaryResponse> discoverEvents();
}
