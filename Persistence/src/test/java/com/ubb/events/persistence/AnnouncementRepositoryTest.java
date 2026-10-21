package com.ubb.events.persistence;

import com.ubb.events.domain.Announcement;
import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AnnouncementRepositoryTest {
    @Autowired private AnnouncementRepository repository;
    @Autowired private TestEntityManager entityManager;
    @Autowired private EventRepository eventRepository;
    @Autowired private OrganizationRepository organizationRepository;

    @Test
    void repositoryIsMarkedAsSpringRepository() {
        assertTrue(AnnouncementRepository.class.isAnnotationPresent(Repository.class));
    }

    @Test
    void savesAndReloadsAnnouncement() {
        Event event = saveEvent();
        Announcement saved = repository.saveAndFlush(new Announcement(null, event, "Title", "Content"));
        assertNotNull(saved.getId());
        entityManager.clear();
        Announcement reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(event.getId(), reloaded.getEvent().getId());
        assertEquals("Title", reloaded.getTitle());
        assertEquals("Content", reloaded.getContent());
    }

    @Test
    void rejectsNullRequiredValues() {
        Event event = saveEvent();
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(new Announcement(null, null, "Title", "Content")));
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(new Announcement(null, event, null, "Content")));
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(new Announcement(null, event, "Title", null)));
    }

    private Event saveEvent() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        return eventRepository.saveAndFlush(new Event(null, organization, 1L, "Hackathon", "Coding night",
                LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), null, null, null, EventStatus.DRAFT));
    }
}
