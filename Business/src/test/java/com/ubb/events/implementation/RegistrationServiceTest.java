package com.ubb.events.implementation;

import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.*;
import com.ubb.events.persistence.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {
    private final EventRepository events = mock(EventRepository.class);
    private final RegistrationRepository registrations = mock(RegistrationRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final RegistrationServiceImpl service = new RegistrationServiceImpl(events, registrations, users);
    private Event event(EventStatus status, LocalDate date, LocalTime start, LocalTime end, long capacity) {
        Event e = new Event(); e.setId(1L); e.setStatus(status); e.setDate(date); e.setStartTime(start); e.setEndTime(end); e.setCapacity(capacity); return e;
    }
    @Test void registersPublishedOpenEvent() {
        Event e = event(EventStatus.PUBLISHED, LocalDate.now().plusDays(1), LocalTime.of(10,0), LocalTime.of(12,0), 2);
        User u = new User(); u.setId(9L);
        when(events.findWithLockById(1L)).thenReturn(Optional.of(e)); when(users.findById(9L)).thenReturn(Optional.of(u));
        when(registrations.countByEventId(1L)).thenReturn(0L); when(registrations.findByEventIdAndUserId(1L,9L)).thenReturn(Optional.empty());
        Registration saved = new Registration(); saved.setId(5L); saved.setEvent(e); saved.setUser(u); when(registrations.save(any())).thenReturn(saved);
        assertEquals(5L, service.registerForEvent(1L,9L).getId());
    }
    @Test void rejectsUnknownEvent() { when(events.findWithLockById(1L)).thenReturn(Optional.empty()); assertThrows(NotFoundException.class, () -> service.registerForEvent(1L,9L)); }
    @Test void rejectsDuplicate() { Event e=event(EventStatus.PUBLISHED,LocalDate.now().plusDays(1),LocalTime.NOON,LocalTime.of(13,0),2); when(events.findWithLockById(1L)).thenReturn(Optional.of(e)); when(registrations.findByEventIdAndUserId(1L,9L)).thenReturn(Optional.of(new Registration())); assertThrows(ConflictException.class,()->service.registerForEvent(1L,9L)); }
    @Test void rejectsFullEvent() { Event e=event(EventStatus.PUBLISHED,LocalDate.now().plusDays(1),LocalTime.NOON,LocalTime.of(13,0),1); when(events.findWithLockById(1L)).thenReturn(Optional.of(e)); when(registrations.findByEventIdAndUserId(1L,9L)).thenReturn(Optional.empty()); when(registrations.countByEventId(1L)).thenReturn(1L); assertThrows(ConflictException.class,()->service.registerForEvent(1L,9L)); }
    @Test void rejectsCanceledOrDraft() { for(EventStatus s:new EventStatus[]{EventStatus.DRAFT,EventStatus.CANCELED}) { Event e=event(s,LocalDate.now().plusDays(1),LocalTime.NOON,LocalTime.of(13,0),1); when(events.findWithLockById(1L)).thenReturn(Optional.of(e)); assertThrows(ConflictException.class,()->service.registerForEvent(1L,9L)); } }
}
