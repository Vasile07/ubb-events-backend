package com.ubb.events.implementation;

import com.ubb.events.AnnouncementService;
import com.ubb.events.dto.AnnouncementResponse;
import com.ubb.events.dto.CreateAnnouncementRequest;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Announcement;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.persistence.AnnouncementRepository;
import com.ubb.events.persistence.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    private final EventRepository events;
    private final AnnouncementRepository announcements;

    public AnnouncementServiceImpl(EventRepository events, AnnouncementRepository announcements) {
        this.events = events;
        this.announcements = announcements;
    }

    @Override
    @Transactional
    public AnnouncementResponse create(Long organizationId, Long eventId, CreateAnnouncementRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()
                || request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessRuleException("Announcement title and content are required");
        }
        Event event = events.findByIdAndOrganizationId(eventId, organizationId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getStatus() == EventStatus.CANCELED) {
            throw new ConflictException("A canceled event cannot receive announcements");
        }
        Announcement saved = announcements.save(new Announcement(null, event, request.getTitle(), request.getContent()));
        return new AnnouncementResponse(saved.getId(), saved.getEvent().getId(), saved.getTitle(), saved.getContent());
    }
}
