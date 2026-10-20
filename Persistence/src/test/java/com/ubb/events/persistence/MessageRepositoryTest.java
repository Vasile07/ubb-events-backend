package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Message;
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
class MessageRepositoryTest {
    @Autowired private MessageRepository repository;
    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private OrganizationRepository organizationRepository;

    @Test
    void savesAndReloadsMessageWithEventSenderAndContent() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "message@example.com", "password"));
        Event event = saveEvent();
        Message saved = repository.saveAndFlush(new Message(null, event, user, "Hello"));

        assertNotNull(saved.getId());
        entityManager.clear();
        Message reloaded = repository.findById(saved.getId()).orElseThrow();
        assertNotNull(reloaded.getEvent());
        assertEquals(event.getId(), reloaded.getEvent().getId());
        assertEquals(user.getId(), reloaded.getSender().getId());
        assertEquals("Hello", reloaded.getContent());
    }

    @Test
    void rejectsNullRequiredValues() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "message2@example.com", "password"));
        Event event = saveEvent();
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Message(null, null, user, "Hello")));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Message(null, event, null, "Hello")));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Message(null, event, user, null)));
    }

    private Event saveEvent() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        return eventRepository.saveAndFlush(new Event(null, organization, 1L, "Hackathon", "Coding night",
                LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), null, null, null, EventStatus.DRAFT));
    }
}
