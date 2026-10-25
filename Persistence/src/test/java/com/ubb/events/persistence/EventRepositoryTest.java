package com.ubb.events.persistence;

import com.ubb.events.domain.Event;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.domain.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Test
    void withoutCriteriaReturnsAllPublishedEventsChronologically() {
        Organization cs = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Organization math = organizationRepository.saveAndFlush(new Organization(null, "UBB Math", null, null));
        Event later = repository.saveAndFlush(event(cs, "Later", DATE.plusDays(1), START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(cs, "Draft", DATE, START, EventStatus.DRAFT));
        Event evening = repository.saveAndFlush(event(math, "Evening", DATE, START.plusHours(2), EventStatus.PUBLISHED));
        repository.saveAndFlush(event(math, "Canceled", DATE, START, EventStatus.CANCELED));
        Event morning = repository.saveAndFlush(event(cs, "Morning", DATE, START.minusHours(8), EventStatus.PUBLISHED));

        assertEquals(List.of(morning.getId(), evening.getId(), later.getId()), discover());
    }

    @Test
    void searchMatchesEventTitleCaseInsensitively() {
        Organization cs = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Event hackathon = repository.saveAndFlush(event(cs, "Spring Hackathon", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(cs, "Workshop", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(cs, "Draft hackathon", DATE, START, EventStatus.DRAFT));

        assertEquals(List.of(hackathon.getId()), discover(EventSpecifications.matchesSearch("HACK")));
    }

    @Test
    void searchMatchesOrganizationName() {
        Organization robotics = organizationRepository.saveAndFlush(new Organization(null, "Robotics Club", null, null));
        Organization math = organizationRepository.saveAndFlush(new Organization(null, "UBB Math", null, null));
        Event meetup = repository.saveAndFlush(event(robotics, "Meetup", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(math, "Quiz", DATE, START, EventStatus.PUBLISHED));

        assertEquals(List.of(meetup.getId()), discover(EventSpecifications.matchesSearch("robotics")));
    }

    @Test
    void searchTreatsWildcardsLiterally() {
        Organization cs = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Event discount = repository.saveAndFlush(event(cs, "100% fun_night", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(cs, "100 fun night", DATE, START, EventStatus.PUBLISHED));

        assertEquals(List.of(discount.getId()), discover(EventSpecifications.matchesSearch("0% fun_")));
    }

    @Test
    void filtersByOrganization() {
        Organization cs = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Organization math = organizationRepository.saveAndFlush(new Organization(null, "UBB Math", null, null));
        Event csEvent = repository.saveAndFlush(event(cs, "CS event", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(math, "Math event", DATE, START, EventStatus.PUBLISHED));

        assertEquals(List.of(csEvent.getId()), discover(EventSpecifications.inOrganization(cs.getId())));
        assertEquals(List.of(), discover(EventSpecifications.inOrganization(-1L)));
    }

    @Test
    void filtersByInclusiveDateRange() {
        Organization cs = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Event before = repository.saveAndFlush(event(cs, "Before", DATE.minusDays(1), START, EventStatus.PUBLISHED));
        Event first = repository.saveAndFlush(event(cs, "First day", DATE, START, EventStatus.PUBLISHED));
        Event last = repository.saveAndFlush(event(cs, "Last day", DATE.plusDays(2), START, EventStatus.PUBLISHED));
        Event after = repository.saveAndFlush(event(cs, "After", DATE.plusDays(3), START, EventStatus.PUBLISHED));

        assertEquals(List.of(first.getId(), last.getId()),
                discover(EventSpecifications.onOrAfter(DATE), EventSpecifications.onOrBefore(DATE.plusDays(2))));
        assertEquals(List.of(first.getId(), last.getId(), after.getId()), discover(EventSpecifications.onOrAfter(DATE)));
        assertEquals(List.of(before.getId(), first.getId()), discover(EventSpecifications.onOrBefore(DATE)));
    }

    @Test
    void combinesAllCriteria() {
        Organization cs = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Organization math = organizationRepository.saveAndFlush(new Organization(null, "UBB Math", null, null));
        Event match = repository.saveAndFlush(event(cs, "Hackathon", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(cs, "Hackathon", DATE.plusDays(10), START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(math, "Hackathon", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(cs, "Workshop", DATE, START, EventStatus.PUBLISHED));

        assertEquals(List.of(match.getId()), discover(
                EventSpecifications.matchesSearch("hack"),
                EventSpecifications.inOrganization(cs.getId()),
                EventSpecifications.onOrAfter(DATE),
                EventSpecifications.onOrBefore(DATE.plusDays(1))));
    }

    @Test
    void searchIsTrimmed() {
        Organization cs = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        Event hackathon = repository.saveAndFlush(event(cs, "Hackathon", DATE, START, EventStatus.PUBLISHED));
        repository.saveAndFlush(event(cs, "Workshop", DATE, START, EventStatus.PUBLISHED));

        assertEquals(List.of(hackathon.getId()), discover(EventSpecifications.matchesSearch("  hack ")));
    }

    @SafeVarargs
    private List<Long> discover(Specification<Event>... criteria) {
        entityManager.clear();
        Specification<Event> specification = EventSpecifications.isPublished();
        for (Specification<Event> criterion : criteria) {
            specification = specification == null ? criterion : specification.and(criterion);
        }
        return repository.findAll(specification, Sort.by("date", "startTime")).stream().map(Event::getId).toList();
    }

    private static Event event(Organization organization, String title, LocalDate date, LocalTime startTime, EventStatus status) {
        return new Event(null, organization, 1L, title, "Description", date, startTime, null, null, null, status);
    }

    private void assertRejected(Event event) {
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(event));
    }
}
