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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnnouncementServiceTest {
    @Test void createsAnnouncementForOrganizationEvent() {
        Event event = new Event(); event.setId(3L); event.setStatus(EventStatus.PUBLISHED);
        EventRepository events = mock(EventRepository.class); AnnouncementRepository announcements = mock(AnnouncementRepository.class);
        when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        when(announcements.save(any())).thenAnswer(inv -> { Announcement a = inv.getArgument(0); a.setId(9L); return a; });
        var response = new AnnouncementServiceImpl(events, announcements).create(7L, 3L, new CreateAnnouncementRequest("Title", "Content"));
        assertEquals(9L, response.getId()); assertEquals(3L, response.getEventId());
        verify(events).findByIdAndOrganizationId(3L, 7L);
    }

    @Test void rejectsCanceledEvent() {
        Event event = new Event(); event.setStatus(EventStatus.CANCELED);
        EventRepository events = mock(EventRepository.class); when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.of(event));
        assertThrows(ConflictException.class, () -> new AnnouncementServiceImpl(events, mock(AnnouncementRepository.class)).create(7L, 3L, new CreateAnnouncementRequest("T", "C")));
    }

    @Test void rejectsMissingEvent() {
        EventRepository events = mock(EventRepository.class); when(events.findByIdAndOrganizationId(3L, 7L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> new AnnouncementServiceImpl(events, mock(AnnouncementRepository.class)).create(7L, 3L, new CreateAnnouncementRequest("T", "C")));
    }
}
