package com.ubb.events.implementation;

import com.ubb.events.FollowUpService;
import com.ubb.events.dto.CreateFollowUpRequest;
import com.ubb.events.dto.FollowUpResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.FollowUpRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.FollowUp;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FollowUpServiceImpl implements FollowUpService {
    private final EventRepository events;
    private final FollowUpRepository followUps;
    private final Clock clock;

    public FollowUpServiceImpl(EventRepository events, FollowUpRepository followUps, Clock clock) {
        this.events = events;
        this.followUps = followUps;
        this.clock = clock;
    }

    @Override
    @Transactional
    public FollowUpResponse createFollowUp(Long organizationId, Long eventId, CreateFollowUpRequest request) {
        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessRuleException("Follow-up content is required");
        }
        Event event = events.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getStatus() == EventStatus.CANCELED) {
            throw new ConflictException("Follow-up cannot be created for a canceled event");
        }
        if (event.getDate() == null || event.getEndTime() == null
                || !LocalDateTime.of(event.getDate(), event.getEndTime())
                .isBefore(LocalDateTime.now(clock))) {
            throw new ConflictException("Event is not completed");
        }
        FollowUp saved = followUps.save(new FollowUp(null, event, request.getContent()));
        return new FollowUpResponse(saved.getId(), event.getId(), saved.getContent());
    }

    @Override
    public FollowUpResponse getFollowUp(Long organizationId, Long eventId) {
        FollowUp followUp = followUps.findByEvent_IdAndEvent_Organization_Id(eventId, organizationId)
                .orElseThrow(() -> new NotFoundException("Follow-up not found"));
        return new FollowUpResponse(followUp.getId(), followUp.getEvent().getId(), followUp.getContent());
    }
}
