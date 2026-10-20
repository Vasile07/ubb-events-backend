package com.ubb.events.persistence;

import com.ubb.events.domain.FollowUp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class FollowUpRepositoryTest {
    @Autowired private FollowUpRepository repository;

    @Test
    void savesAndReloadsFollowUpWithEventIdAndContent() {
        FollowUp saved = repository.saveAndFlush(new FollowUp(null, 42L, "Helpful follow-up"));

        assertNotNull(saved.getId());
        FollowUp reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(42L, reloaded.getEventId());
        assertEquals("Helpful follow-up", reloaded.getContent());
    }

    @Test
    void rejectsNullRequiredValues() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new FollowUp(null, null, "content")));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new FollowUp(null, 42L, null)));
    }
}
