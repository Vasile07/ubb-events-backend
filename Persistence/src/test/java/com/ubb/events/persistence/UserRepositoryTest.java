package com.ubb.events.persistence;

import com.ubb.events.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository repository;

    @Test
    void savesUserWithGeneratedIdAndScalarFields() {
        User saved = repository.saveAndFlush(new User(null, "Ada", "Lovelace", "ada@example.com", "hash-1"));
        assertNotNull(saved.getId());
        assertEquals("ada@example.com", repository.findById(saved.getId()).orElseThrow().getEmail());
    }

    @Test
    void rejectsNullRequiredFields() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User(null, null, "Lovelace", "ada@example.com", "hash-1")));
    }

    @Test
    void rejectsDuplicateEmail() {
        repository.saveAndFlush(new User(null, "Ada", "Lovelace", "ada@example.com", "hash-1"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User(null, "Grace", "Hopper", "ada@example.com", "hash-2")));
    }

    @Test
    void rejectsDuplicatePassword() {
        repository.saveAndFlush(new User(null, "Ada", "Lovelace", "ada@example.com", "hash-1"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new User(null, "Grace", "Hopper", "grace@example.com", "hash-1")));
    }
}
