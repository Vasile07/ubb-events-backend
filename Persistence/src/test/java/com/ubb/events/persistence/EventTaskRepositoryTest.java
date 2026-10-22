package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.EventTask;
import com.ubb.events.domain.Organization;
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
class EventTaskRepositoryTest {
    @Autowired private EventTaskRepository repository;
    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private OrganizationRepository organizationRepository;

    @Test
    void savesAndReloadsTaskWithEventAndAssignedUser() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "task@example.com", "password"));
        Event event = saveEvent();
        EventTask saved = repository.saveAndFlush(new EventTask(null, "Book the room", user, event));

        assertNotNull(saved.getId());
        entityManager.clear();
        EventTask reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Book the room", reloaded.getDescription());
        assertNotNull(reloaded.getEvent());
        assertEquals(event.getId(), reloaded.getEvent().getId());
        assertNotNull(reloaded.getAssignedUser());
        assertEquals(user.getId(), reloaded.getAssignedUser().getId());
    }

    @Test
    void savesTaskWithoutAssignedUser() {
        Event event = saveEvent();
        EventTask saved = repository.saveAndFlush(new EventTask(null, "Print posters", null, event));

        entityManager.clear();
        EventTask reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Print posters", reloaded.getDescription());
        assertNotNull(reloaded.getEvent());
        assertNull(reloaded.getAssignedUser());
    }

    @Test
    void rejectsNullRequiredValues() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "task2@example.com", "password"));
        Event event = saveEvent();
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new EventTask(null, null, user, event)));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new EventTask(null, "Book the room", user, null)));
    }

    private Event saveEvent() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        return eventRepository.saveAndFlush(new Event(null, organization, 1L, "Hackathon", "Coding night",
                LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), null, null, null, EventStatus.DRAFT));
    }
}
