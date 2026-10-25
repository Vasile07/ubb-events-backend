package com.ubb.events;

import com.ubb.events.dto.EventTaskResponse;
import java.util.List;

public interface EventTaskService {
    List<EventTaskResponse> listEventTasks(Long organizationId, Long eventId);
}
