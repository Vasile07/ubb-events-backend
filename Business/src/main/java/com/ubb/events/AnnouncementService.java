package com.ubb.events;

import com.ubb.events.dto.AnnouncementResponse;
import com.ubb.events.dto.CreateAnnouncementRequest;
import java.util.List;

public interface AnnouncementService {
    AnnouncementResponse create(Long organizationId, Long eventId, CreateAnnouncementRequest request);
    List<AnnouncementResponse> list(Long eventId, Long userId);
}
