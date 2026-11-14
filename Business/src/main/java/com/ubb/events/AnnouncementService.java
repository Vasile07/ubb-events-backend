package com.ubb.events;

import com.ubb.events.dto.AnnouncementResponse;
import com.ubb.events.dto.CreateAnnouncementRequest;

public interface AnnouncementService {
    AnnouncementResponse create(Long organizationId, Long eventId, CreateAnnouncementRequest request);
}
