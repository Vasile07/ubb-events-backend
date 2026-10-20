package com.ubb.events.persistence;

import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MembershipRepositoryTest {
    @Autowired private MembershipRepository repository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrganizationRepository organizationRepository;

    @Test
    void savesAndReloadsMembershipWithBothStatusValuesAndNullableFields() {
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "ana@example.com", "password"));
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));

        Membership pending = repository.saveAndFlush(new Membership(null, user, organization, MembershipRole.MEMBER, MembershipStatus.PENDING));
        Membership active = repository.saveAndFlush(new Membership(null, user, organization, null, MembershipStatus.ACTIVE));

        assertNotNull(pending.getId());
        assertNotNull(active.getId());
        assertEquals(MembershipStatus.PENDING, repository.findById(pending.getId()).orElseThrow().getStatus());
        assertEquals(MembershipStatus.ACTIVE, repository.findById(active.getId()).orElseThrow().getStatus());
        assertNull(repository.findById(active.getId()).orElseThrow().getRole());
    }

    @Test
    void rejectsNullRequiredAssociations() {
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        User user = userRepository.saveAndFlush(new User(null, "Ana", "Student", "ana2@example.com", "password2"));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Membership(null, null, organization, null, null)));
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new Membership(null, user, null, null, null)));
    }

    @Test
    void findsOnlyMembershipsForRequestedUser() {
        User ana = userRepository.saveAndFlush(new User(null, "Ana", "Student", "ana-list@example.com", "password"));
        User bob = userRepository.saveAndFlush(new User(null, "Bob", "Student", "bob-list@example.com", "password"));
        Organization organization = organizationRepository.saveAndFlush(new Organization(null, "UBB CS", null, null));
        repository.saveAndFlush(new Membership(null, ana, organization, MembershipRole.MEMBER, MembershipStatus.ACTIVE));
        repository.saveAndFlush(new Membership(null, bob, organization, MembershipRole.MANAGER, MembershipStatus.ACTIVE));

        assertEquals(1, repository.findAllByUserId(ana.getId()).size());
        assertEquals(ana.getId(), repository.findAllByUserId(ana.getId()).get(0).getUser().getId());
    }
}
