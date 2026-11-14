package com.ubb.events;

import com.ubb.events.dto.EventTaskRequest;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.dto.MyTaskResponse;
import java.util.List;

public interface EventTaskService {
    List<EventTaskResponse> listEventTasks(Long organizationId, Long eventId);
    List<MyTaskResponse> listMyTasks(Long userId);
    EventTaskResponse addTask(Long organizationId, Long eventId, EventTaskRequest request);
    EventTaskResponse updateTask(Long organizationId, Long eventId, Long taskId, EventTaskRequest request);
    void removeTask(Long organizationId, Long eventId, Long taskId);
}
