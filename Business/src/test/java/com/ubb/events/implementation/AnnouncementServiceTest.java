package com.ubb.events.implementation;

import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Announcement;
import com.ubb.events.dto.CreateAnnouncementRequest;
import com.ubb.events.persistence.AnnouncementRepository;
import com.ubb.events.persistence.EventRepository;
import java.util.Optional;
import java.util.List;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.dto.AnnouncementResponse;
import com.ubb.events.persistence.RegistrationRepository;
import com.ubb.events.persistence.MembershipRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnnouncementServiceTest {
    @Test void listsAnnouncementsForRegisteredParticipant() {
        Event event = new Event(); event.setId(3L); Organization org = new Organization(); org.setId(7L); event.setOrganization(org);
        Announcement a = new Announcement(9L, event, "Title", "Content");
        EventRepository events = mock(EventRepository.class); AnnouncementRepository announcements = mock(AnnouncementRepository.class);
        RegistrationRepository registrations = mock(RegistrationRepository.class); MembershipRepository memberships = mock(MembershipRepository.class);
        when(events.findById(3L)).thenReturn(Optional.of(event)); when(registrations.existsByEventIdAndUserId(3L, 42L)).thenReturn(true);
        when(announcements.findAllByEventIdOrderByIdAsc(3L)).thenReturn(List.of(a));
        List<AnnouncementResponse> result = new AnnouncementServiceImpl(events, announcements, registrations, memberships).list(3L, 42L);
        assertEquals(1, result.size()); assertEquals("Content", result.get(0).getContent()); assertEquals(3L, result.get(0).getEventId());
    }

    @Test void listsAnnouncementsForActiveMember() {
        Event event = new Event(); event.setId(3L); Organization org = new Organization(); org.setId(7L); event.setOrganization(org);
        EventRepository events = mock(EventRepository.class); AnnouncementRepository announcements = mock(AnnouncementRepository.class); RegistrationRepository registrations = mock(RegistrationRepository.class); MembershipRepository memberships = mock(MembershipRepository.class);
        when(events.findById(3L)).thenReturn(Optional.of(event)); when(registrations.existsByEventIdAndUserId(3L, 42L)).thenReturn(false);
        Membership membership = new Membership(); membership.setStatus(MembershipStatus.ACTIVE); when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(Optional.of(membership));
        when(announcements.findAllByEventIdOrderByIdAsc(3L)).thenReturn(List.of());
        assertTrue(new AnnouncementServiceImpl(events, announcements, registrations, memberships).list(3L, 42L).isEmpty());
    }

    @Test void listsAnnouncementsForActiveManager() {
        Event event = new Event(); event.setId(3L); Organization org = new Organization(); org.setId(7L); event.setOrganization(org);
        EventRepository events = mock(EventRepository.class); AnnouncementRepository announcements = mock(AnnouncementRepository.class); RegistrationRepository registrations = mock(RegistrationRepository.class); MembershipRepository memberships = mock(MembershipRepository.class);
        when(events.findById(3L)).thenReturn(Optional.of(event)); when(registrations.existsByEventIdAndUserId(3L, 43L)).thenReturn(false);
        Membership membership = new Membership(); membership.setStatus(MembershipStatus.ACTIVE); membership.setRole(MembershipRole.MANAGER); when(memberships.findByUserIdAndOrganizationId(43L, 7L)).thenReturn(Optional.of(membership));
        when(announcements.findAllByEventIdOrderByIdAsc(3L)).thenReturn(List.of());
        assertTrue(new AnnouncementServiceImpl(events, announcements, registrations, memberships).list(3L, 43L).isEmpty());
    }

    @Test void deniesUserWithoutMembershipOrRegistration() {
        Event event = new Event(); event.setId(3L); Organization org = new Organization(); org.setId(7L); event.setOrganization(org);
        EventRepository events = mock(EventRepository.class); AnnouncementRepository announcements = mock(AnnouncementRepository.class); RegistrationRepository registrations = mock(RegistrationRepository.class); MembershipRepository memberships = mock(MembershipRepository.class);
        when(events.findById(3L)).thenReturn(Optional.of(event)); when(registrations.existsByEventIdAndUserId(3L, 42L)).thenReturn(false); when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(Optional.empty());
        assertThrows(com.ubb.events.common.errors.ForbiddenException.class, () -> new AnnouncementServiceImpl(events, announcements, registrations, memberships).list(3L, 42L));
    }

    @Test void rejectsUnknownEvent() {
        EventRepository events = mock(EventRepository.class); when(events.findById(3L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> new AnnouncementServiceImpl(events, mock(AnnouncementRepository.class), mock(RegistrationRepository.class), mock(MembershipRepository.class)).list(3L, 42L));
    }

    @Test void pendingMembershipDoesNotGrantAccess() {
        Event event = new Event(); event.setId(3L); Organization org = new Organization(); org.setId(7L); event.setOrganization(org);
        EventRepository events = mock(EventRepository.class); AnnouncementRepository announcements = mock(AnnouncementRepository.class); RegistrationRepository registrations = mock(RegistrationRepository.class); MembershipRepository memberships = mock(MembershipRepository.class);
        when(events.findById(3L)).thenReturn(Optional.of(event)); when(registrations.existsByEventIdAndUserId(3L, 42L)).thenReturn(false);
        Membership membership = new Membership(); membership.setStatus(MembershipStatus.PENDING); when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(Optional.of(membership));
        assertThrows(com.ubb.events.common.errors.ForbiddenException.class, () -> new AnnouncementServiceImpl(events, announcements, registrations, memberships).list(3L, 42L));
    }
    @Test void createsAnnouncementForOrganizationEvent() {
        Event event = new Event(); event.setId(3L); event.setStatus(EventStatus.PUBLISHED);
        EventRepository events = mock(EventRepository.class); AnnouncementRepository announcements = mock(AnnouncementRepository.class);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        when(announcements.save(any())).thenAnswer(inv -> { Announcement a = inv.getArgument(0); a.setId(9L); return a; });
        var response = new AnnouncementServiceImpl(events, announcements, mock(RegistrationRepository.class), mock(MembershipRepository.class)).create(7L, 3L, new CreateAnnouncementRequest("Title", "Content"));
        assertEquals(9L, response.getId()); assertEquals(3L, response.getEventId());
        verify(events).findByIdAndOrganizationId(3L, 7L);
    }

    @Test void rejectsCanceledEvent() {
        Event event = new Event(); event.setStatus(EventStatus.CANCELED);
        EventRepository events = mock(EventRepository.class); when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        assertThrows(ConflictException.class, () -> new AnnouncementServiceImpl(events, mock(AnnouncementRepository.class), mock(RegistrationRepository.class), mock(MembershipRepository.class)).create(7L, 3L, new CreateAnnouncementRequest("T", "C")));
    }

    @Test void rejectsMissingEvent() {
        EventRepository events = mock(EventRepository.class); when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> new AnnouncementServiceImpl(events, mock(AnnouncementRepository.class), mock(RegistrationRepository.class), mock(MembershipRepository.class)).create(7L, 3L, new CreateAnnouncementRequest("T", "C")));
    }
}
