package com.ubb.events.persistence;

import com.ubb.events.domain.Message;
import com.ubb.events.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {
    @Autowired private MessageRepository repository;
    @Autowired private UserRepository userRepository;

    @Test
    void savesAndReloadsMessageWithEventIdSenderAndContent() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "message@example.com", "password"));
        Message saved = repository.saveAndFlush(new Message(null, 42L, user, "Hello"));

        assertNotNull(saved.getId());
        Message reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(42L, reloaded.getEventId());
        assertEquals(user.getId(), reloaded.getSender().getId());
        assertEquals("Hello", reloaded.getContent());
    }

    @Test
    void rejectsNullRequiredValues() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "message2@example.com", "password"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Message(null, null, user, "Hello")));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Message(null, 42L, null, "Hello")));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Message(null, 42L, user, null)));
    }
}
