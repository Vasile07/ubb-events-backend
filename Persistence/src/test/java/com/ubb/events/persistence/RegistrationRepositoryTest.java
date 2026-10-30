package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.Registration;
import com.ubb.events.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RegistrationRepositoryTest {
    @Autowired private RegistrationRepository repository;
    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private OrganizationRepository organizationRepository;

    @Test
    void savesAndReloadsRegistrationWithEventAndUser() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "registration@example.com", "password"));
        Event event = saveEvent();
        Registration saved = repository.saveAndFlush(new Registration(null, event, user));

        assertNotNull(saved.getId());
        entityManager.clear();
        Registration reloaded = repository.findById(saved.getId()).orElseThrow();
        assertNotNull(reloaded.getEvent());
        assertEquals(event.getId(), reloaded.getEvent().getId());
        assertEquals(user.getId(), reloaded.getUser().getId());
    }

    @Test
    void rejectsNullRequiredValues() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "registration2@example.com", "password"));
        Event event = saveEvent();
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Registration(null, null, user)));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Registration(null, event, null)));
    }

    @Test
    void countsEventRegistrationsAndFindsUserRegistration() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "registration3@example.com", "password"));
        Event event = saveEvent();
        repository.saveAndFlush(new Registration(null, event, user));

        assertEquals(1L, repository.countByEventId(event.getId()));
        assertTrue(repository.existsByEventIdAndUserId(event.getId(), user.getId()));
        assertFalse(repository.existsByEventIdAndUserId(event.getId(), 999L));
    }

    @Test
    void deletingOneUsersRegistrationLeavesAnotherUsersRegistration() {
        User first = userRepository.saveAndFlush(new User(null, "Ana", "First", "registration-delete-1@example.com", "password"));
        User second = userRepository.saveAndFlush(new User(null, "Ana", "Second", "registration-delete-2@example.com", "password"));
        Event event = saveEvent();
        Registration firstRegistration = repository.saveAndFlush(new Registration(null, event, first));
        Registration secondRegistration = repository.saveAndFlush(new Registration(null, event, second));

        repository.delete(firstRegistration);
        repository.flush();

        assertFalse(repository.findByEventIdAndUserId(event.getId(), first.getId()).isPresent());
        assertEquals(secondRegistration.getId(), repository.findByEventIdAndUserId(event.getId(), second.getId()).orElseThrow().getId());
        assertEquals(1L, repository.countByEventId(event.getId()));
    }

    private Event saveEvent() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        return eventRepository.saveAndFlush(new Event(null, organization, 1L, "Hackathon", "Coding night",
                LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), null, null, null, EventStatus.DRAFT));
    }

    @Test
    void findsOnlyRequestedUserAndOrdersByEventDate() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        User requested = userRepository.saveAndFlush(new User(null, "Requested", "User", "requested@example.com", "hash"));
        User other = userRepository.saveAndFlush(new User(null, "Other", "User", "other@example.com", "hash"));
        Event later = eventRepository.saveAndFlush(new Event(null, organization, 1L, "Later", "Description",
                LocalDate.of(2026, 12, 2), LocalTime.NOON, LocalTime.of(13, 0), "Room", 20L, EventStatus.PUBLISHED));
        Event earlier = eventRepository.saveAndFlush(new Event(null, organization, 1L, "Earlier", "Description",
                LocalDate.of(2026, 12, 1), LocalTime.NOON, LocalTime.of(13, 0), "Room", 20L, EventStatus.PUBLISHED));
        repository.saveAndFlush(new Registration(null, later, requested));
        repository.saveAndFlush(new Registration(null, earlier, requested));
        repository.saveAndFlush(new Registration(null, earlier, other));

        var result = repository.findAllByUserId(requested.getId());

        assertEquals(2, result.size());
        assertEquals("Earlier", result.get(0).getEvent().getTitle());
        assertEquals("Later", result.get(1).getEvent().getTitle());
        assertEquals(requested.getId(), result.get(0).getUser().getId());
        assertEquals(requested.getId(), result.get(1).getUser().getId());
    }

    @Test
    void findsRegistrationsOnlyForRequestedEventAndOrganizationWithJoinedUsers() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB Registration Org", null, null));
        Organization otherOrganization = organizationRepository.saveAndFlush(new Organization(null, "Other Registration Org", null, null));
        User first = userRepository.saveAndFlush(new User(null, "Ada", "Lovelace", "registration-filter-1@example.com", "hash"));
        User second = userRepository.saveAndFlush(new User(null, "Grace", "Hopper", "registration-filter-2@example.com", "hash"));
        Event requestedEvent = eventRepository.saveAndFlush(new Event(null, organization, 1L, "Requested", "Description",
                LocalDate.of(2026, 12, 3), LocalTime.NOON, LocalTime.of(13, 0), "Room", 20L, EventStatus.PUBLISHED));
        Event otherEvent = eventRepository.saveAndFlush(new Event(null, otherOrganization, 1L, "Other", "Description",
                LocalDate.of(2026, 12, 4), LocalTime.NOON, LocalTime.of(13, 0), "Room", 20L, EventStatus.PUBLISHED));
        repository.saveAndFlush(new Registration(null, requestedEvent, first));
        repository.saveAndFlush(new Registration(null, requestedEvent, second));
        repository.saveAndFlush(new Registration(null, otherEvent, first));

        var result = repository.findAllByEventIdAndOrganizationId(requestedEvent.getId(), organization.getId());

        assertEquals(2, result.size());
        assertEquals("Hopper", result.get(0).getUser().getLastName());
        assertEquals("Grace", result.get(0).getUser().getFirstName());
        assertEquals("registration-filter-2@example.com", result.get(0).getUser().getEmail());
        assertEquals("Lovelace", result.get(1).getUser().getLastName());
    }

    @Test
    void returnsEmptyListWhenEventHasNoRegistrations() {
        Event event = saveEvent();
        assertTrue(repository.findAllByEventIdAndOrganizationId(event.getId(), event.getOrganization().getId()).isEmpty());
    }
}
