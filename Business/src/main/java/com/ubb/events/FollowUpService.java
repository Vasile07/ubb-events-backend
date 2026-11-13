package com.ubb.events;

import com.ubb.events.dto.CreateFollowUpRequest;
import com.ubb.events.dto.FollowUpResponse;

public interface FollowUpService {
    FollowUpResponse createFollowUp(Long organizationId, Long eventId, CreateFollowUpRequest request);
    FollowUpResponse getFollowUp(Long organizationId, Long eventId);
}
