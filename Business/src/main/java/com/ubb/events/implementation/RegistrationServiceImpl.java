package com.ubb.events.implementation;

import com.ubb.events.RegistrationService;
import com.ubb.events.dto.RegistrationResponse;
import com.ubb.events.dto.RegistrationDetailsResponse;
import com.ubb.events.dto.EventSummaryResponse;
import com.ubb.events.dto.EventOrganizationSummaryResponse;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.RegistrationRepository;
import com.ubb.events.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Registration;
import com.ubb.events.domain.User;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    public RegistrationServiceImpl(EventRepository eventRepository, RegistrationRepository registrationRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository; this.registrationRepository = registrationRepository; this.userRepository = userRepository;
    }
    @Override @Transactional
    public RegistrationResponse registerForEvent(Long eventId, Long userId) {
        Event event = eventRepository.findWithLockById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getStatus() != EventStatus.PUBLISHED) throw new ConflictException("Event is not published or has been canceled");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(event.getDate(), event.getStartTime());
        LocalDateTime end = LocalDateTime.of(event.getDate(), event.getEndTime());
        if (!start.isAfter(now)) throw new ConflictException("Registration is closed because the event has started");
        if (!end.isAfter(now)) throw new ConflictException("Event is completed");
        if (registrationRepository.findByEventIdAndUserId(eventId, userId).isPresent()) throw new ConflictException("User is already registered");
        if (registrationRepository.countByEventId(eventId) >= event.getCapacity()) throw new ConflictException("Event is full");
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Registration registration = new Registration(); registration.setEvent(event); registration.setUser(user);
        Registration saved = registrationRepository.save(registration);
        return new RegistrationResponse(saved.getId(), eventId, userId);
    }

    @Override @Transactional
    public void cancelRegistration(Long eventId, Long userId) {
        Event event = eventRepository.findWithLockById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        LocalDateTime start = LocalDateTime.of(event.getDate(), event.getStartTime());
        if (!start.isAfter(LocalDateTime.now())) {
            throw new ConflictException("Registration cannot be canceled because the event has started");
        }
        Registration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Active registration not found"));
        registrationRepository.delete(registration);
    }

    @Override
    public List<RegistrationDetailsResponse> listMyRegistrations(Long userId) {
        return registrationRepository.findAllByUserId(userId).stream()
                .map(registration -> new RegistrationDetailsResponse(registration.getId(), toEventSummary(registration.getEvent())))
                .toList();
    }

    private static EventSummaryResponse toEventSummary(Event event) {
        return new EventSummaryResponse(event.getId(), event.getTitle(), event.getDate(), event.getStartTime(),
                event.getEndTime(), event.getLocation(), event.getStatus(),
                new EventOrganizationSummaryResponse(event.getOrganization().getId(), event.getOrganization().getName()));
    }
}
