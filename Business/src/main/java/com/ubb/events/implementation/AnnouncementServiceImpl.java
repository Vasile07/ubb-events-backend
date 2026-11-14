package com.ubb.events.implementation;

import com.ubb.events.AnnouncementService;
import com.ubb.events.dto.AnnouncementResponse;
import com.ubb.events.dto.CreateAnnouncementRequest;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.errors.ForbiddenException;
import com.ubb.events.domain.Announcement;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.persistence.AnnouncementRepository;
import com.ubb.events.persistence.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.ubb.events.persistence.RegistrationRepository;
import com.ubb.events.persistence.MembershipRepository;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    private final EventRepository events;
    private final AnnouncementRepository announcements;
    private final RegistrationRepository registrations;
    private final MembershipRepository memberships;

    @Autowired
    public AnnouncementServiceImpl(EventRepository events, AnnouncementRepository announcements, RegistrationRepository registrations, MembershipRepository memberships) {
        this.events = events; this.announcements = announcements; this.registrations = registrations; this.memberships = memberships;
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

    @Override
    public List<AnnouncementResponse> list(Long eventId, Long userId) {
        Event event = events.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        boolean participant = registrations.existsByEventIdAndUserId(eventId, userId);
        boolean member = memberships.findByUserIdAndOrganizationId(userId, event.getOrganization().getId())
                .map(m -> m.getStatus() == MembershipStatus.ACTIVE).orElse(false);
        if (!participant && !member) throw new ForbiddenException("You do not have access to this event's announcements");
        return announcements.findAllByEventIdOrderByIdAsc(eventId).stream()
                .map(a -> new AnnouncementResponse(a.getId(), a.getEvent().getId(), a.getTitle(), a.getContent())).toList();
    }
}
