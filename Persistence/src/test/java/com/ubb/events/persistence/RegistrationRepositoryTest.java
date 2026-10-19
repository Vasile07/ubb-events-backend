package com.ubb.events.persistence;

import com.ubb.events.domain.Registration;
import com.ubb.events.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RegistrationRepositoryTest {
    @Autowired private RegistrationRepository repository;
    @Autowired private UserRepository userRepository;

    @Test
    void savesAndReloadsRegistrationWithEventIdAndUser() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "registration@example.com", "password"));
        Registration saved = repository.saveAndFlush(new Registration(null, 42L, user));

        assertNotNull(saved.getId());
        Registration reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(42L, reloaded.getEventId());
        assertEquals(user.getId(), reloaded.getUser().getId());
    }

    @Test
    void rejectsNullRequiredValues() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "registration2@example.com", "password"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Registration(null, null, user)));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Registration(null, 42L, null)));
    }
}
