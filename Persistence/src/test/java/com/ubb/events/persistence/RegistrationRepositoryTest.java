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
}
