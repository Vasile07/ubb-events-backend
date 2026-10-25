package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EventRepositoryTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);
    private static final LocalTime START = LocalTime.of(18, 0);

    @Autowired private EventRepository repository;
    @Autowired private TestEntityManager entityManager;
    @Autowired private OrganizationRepository organizationRepository;

    @Test
    void savesAndReloadsEventWithOrganizationAndNullableFields() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Event saved = repository.saveAndFlush(new Event(null, organization, 7L, "Hackathon", "Coding night",
                DATE, START, null, null, null, EventStatus.DRAFT));

        assertNotNull(saved.getId());
        entityManager.clear();
        Event reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(organization.getId(), reloaded.getOrganization().getId());
        assertEquals(7L, reloaded.getCategoryId());
        assertEquals("Hackathon", reloaded.getTitle());
        assertEquals("Coding night", reloaded.getDescription());
        assertEquals(DATE, reloaded.getDate());
        assertEquals(START, reloaded.getStartTime());
        assertNull(reloaded.getEndTime());
        assertNull(reloaded.getLocation());
        assertNull(reloaded.getCapacity());
        assertEquals(EventStatus.DRAFT, reloaded.getStatus());
    }

    @Test
    void savesAndReloadsOptionalFieldsWhenPresent() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB Math", null, null));
        Event saved = repository.saveAndFlush(new Event(null, organization, 7L, "Workshop", "Spring Boot",
                DATE, START, LocalTime.of(20, 0), "FSEGA Room 1", 50L, EventStatus.PUBLISHED));

        entityManager.clear();
        Event reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(LocalTime.of(20, 0), reloaded.getEndTime());
        assertEquals("FSEGA Room 1", reloaded.getLocation());
        assertEquals(50L, reloaded.getCapacity());
        assertEquals(EventStatus.PUBLISHED, reloaded.getStatus());
    }

    @Test
    void rejectsNullRequiredValues() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB Physics", null, null));
        assertRejected(new Event(null, null, 7L, "Title", "Description", DATE, START, null, null, null, EventStatus.DRAFT));
        assertRejected(new Event(null, organization, null, "Title", "Description", DATE, START, null, null, null, EventStatus.DRAFT));
        assertRejected(new Event(null, organization, 7L, null, "Description", DATE, START, null, null, null, EventStatus.DRAFT));
        assertRejected(new Event(null, organization, 7L, "Title", null, DATE, START, null, null, null, EventStatus.DRAFT));
        assertRejected(new Event(null, organization, 7L, "Title", "Description", null, START, null, null, null, EventStatus.DRAFT));
        assertRejected(new Event(null, organization, 7L, "Title", "Description", DATE, null, null, null, null, EventStatus.DRAFT));
        assertRejected(new Event(null, organization, 7L, "Title", "Description", DATE, START, null, null, null, null));
    }

    @Test
    void findsOnlyEventsOfOrganizationOrderedByDateAndStartTime() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Organization other = organizationRepository.saveAndFlush(new Organization(null, "UBB Math", null, null));
        Event late = repository.saveAndFlush(event(organization, "Late", DATE.plusDays(1), START, EventStatus.CANCELED));
        Event evening = repository.saveAndFlush(event(organization, "Evening", DATE, START.plusHours(2), EventStatus.PUBLISHED));
        Event morning = repository.saveAndFlush(event(organization, "Morning", DATE, START.minusHours(8), EventStatus.DRAFT));
        repository.saveAndFlush(event(other, "Other organization", DATE, START, EventStatus.PUBLISHED));

        entityManager.clear();
        List<Event> events = repository.findAllByOrganizationIdOrderByDateAscStartTimeAsc(organization.getId());

        assertEquals(List.of(morning.getId(), evening.getId(), late.getId()),
                events.stream().map(Event::getId).toList());
    }

    @Test
    void findsEventByIdOnlyWithinItsOrganization() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Organization other = organizationRepository.saveAndFlush(new Organization(null, "UBB Math", null, null));
        Event saved = repository.saveAndFlush(event(organization, "Hackathon", DATE, START, EventStatus.DRAFT));

        entityManager.clear();
        assertEquals(saved.getId(),
                repository.findByIdAndOrganizationId(saved.getId(), organization.getId()).orElseThrow().getId());
        assertTrue(repository.findByIdAndOrganizationId(saved.getId(), other.getId()).isEmpty());
    }

    private static Event event(Organization organization, String title, LocalDate date, LocalTime startTime, EventStatus status) {
        return new Event(null, organization, 1L, title, "Description", date, startTime, null, null, null, status);
    }

    private void assertRejected(Event event) {
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(event));
    }
}
