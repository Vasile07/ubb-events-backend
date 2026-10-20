package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.FollowUp;
import com.ubb.events.domain.Organization;
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
class FollowUpRepositoryTest {
    @Autowired private FollowUpRepository repository;
    @Autowired private TestEntityManager entityManager;
    @Autowired private EventRepository eventRepository;
    @Autowired private OrganizationRepository organizationRepository;

    @Test
    void savesAndReloadsFollowUpWithEventAndContent() {
        Event event = saveEvent();
        FollowUp saved = repository.saveAndFlush(new FollowUp(null, event, "Helpful follow-up"));

        assertNotNull(saved.getId());
        entityManager.clear();
        FollowUp reloaded = repository.findById(saved.getId()).orElseThrow();
        assertNotNull(reloaded.getEvent());
        assertEquals(event.getId(), reloaded.getEvent().getId());
        assertEquals("Helpful follow-up", reloaded.getContent());
    }

    @Test
    void rejectsNullRequiredValues() {
        Event event = saveEvent();
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new FollowUp(null, null, "content")));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new FollowUp(null, event, null)));
    }

    private Event saveEvent() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        return eventRepository.saveAndFlush(new Event(null, organization, 1L, "Hackathon", "Coding night",
                LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), null, null, null, EventStatus.DRAFT));
    }
}
