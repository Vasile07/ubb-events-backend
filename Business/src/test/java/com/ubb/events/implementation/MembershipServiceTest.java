package com.ubb.events.implementation;

import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.User;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.dto.MembershipDecisionRequest;
import com.ubb.events.dto.MembershipDecisionResponse;
import com.ubb.events.dto.MembershipDecisionStatus;
import com.ubb.events.dto.OrganizationMemberResponse;
import com.ubb.events.domain.MembershipDecision;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {
    @Mock MembershipRepository memberships;
    @Mock com.ubb.events.persistence.OrganizationRepository organizations;
    @Mock com.ubb.events.persistence.UserRepository users;
    @InjectMocks MembershipServiceImpl service;

    @Test
    void createsPendingMemberRequestForAuthenticatedUser() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        when(organizations.findById(7L)).thenReturn(java.util.Optional.of(organization));
        when(users.findById(42L)).thenReturn(java.util.Optional.of(user));
        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(java.util.Optional.empty());
        service.requestMembership(7L, 42L);
        verify(memberships).save(argThat(m -> m.getUser() == user && m.getOrganization() == organization
                && m.getRole() == MembershipRole.MEMBER && m.getStatus() == MembershipStatus.PENDING));
    }

    @Test
    void rejectsDuplicatePendingRequest() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        when(organizations.findById(7L)).thenReturn(java.util.Optional.of(organization));
        when(users.findById(42L)).thenReturn(java.util.Optional.of(user));
        when(memberships.findByUserIdAndOrganizationId(42L, 7L))
                .thenReturn(java.util.Optional.of(new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.PENDING)));

        assertThrows(ConflictException.class, () -> service.requestMembership(7L, 42L));
        verify(memberships, never()).save(any());
    }

    @Test
    void rejectsAlreadyActiveMember() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        when(organizations.findById(7L)).thenReturn(java.util.Optional.of(organization));
        when(users.findById(42L)).thenReturn(java.util.Optional.of(user));
        when(memberships.findByUserIdAndOrganizationId(42L, 7L))
                .thenReturn(java.util.Optional.of(new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.ACTIVE)));

        assertThrows(BusinessRuleException.class, () -> service.requestMembership(7L, 42L));
        verify(memberships, never()).save(any());
    }

    @Test
    void rejectsUnknownOrganization() {
        when(organizations.findById(7L)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> service.requestMembership(7L, 42L));
        verifyNoInteractions(users, memberships);
    }

    @Test
    void rejectsUnknownUser() {
        when(organizations.findById(7L)).thenReturn(java.util.Optional.of(new Organization(7L, "Students", null, null)));
        when(users.findById(42L)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> service.requestMembership(7L, 42L));
        verifyNoInteractions(memberships);
    }

    @Test
    void defaultsMissingStatusToPendingAndMapsRequests() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        when(memberships.findAllByOrganizationIdAndStatus(7L, MembershipStatus.PENDING))
                .thenReturn(List.of(new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.PENDING)));

        List<MembershipRequestResponse> result = service.listMembershipRequests(7L, null);

        assertEquals(List.of(new MembershipRequestResponse(11L, 42L, MembershipRole.MEMBER, MembershipStatus.PENDING)), result);
        verify(memberships).findAllByOrganizationIdAndStatus(7L, MembershipStatus.PENDING);
    }

    @Test
    void usesExplicitStatusAndReturnsEmptyResults() {
        when(memberships.findAllByOrganizationIdAndStatus(7L, MembershipStatus.ACTIVE)).thenReturn(List.of());

        assertTrue(service.listMembershipRequests(7L, MembershipStatus.ACTIVE).isEmpty());
        verify(memberships).findAllByOrganizationIdAndStatus(7L, MembershipStatus.ACTIVE);
    }

    @Test
    void listsActiveMembersWithPublicUserFieldsAndRole() {
        Organization organization = new Organization(7L, "Students", null, null);
        User ana = new User(42L, "Ana", "Student", "ana@example.com", "secret");
        User bob = new User(43L, "Bob", "Manager", "bob@example.com", "secret2");
        when(memberships.findAllByOrganizationIdAndStatus(7L, MembershipStatus.ACTIVE)).thenReturn(List.of(
                new Membership(11L, ana, organization, MembershipRole.MEMBER, MembershipStatus.ACTIVE),
                new Membership(12L, bob, organization, MembershipRole.MANAGER, MembershipStatus.ACTIVE)));

        List<OrganizationMemberResponse> result = service.listOrganizationMembers(7L);

        assertEquals(List.of(
                new OrganizationMemberResponse(42L, "Ana", "Student", "ana@example.com", MembershipRole.MEMBER),
                new OrganizationMemberResponse(43L, "Bob", "Manager", "bob@example.com", MembershipRole.MANAGER)), result);
        assertFalse(result.get(0).toString().contains("secret"));
        verify(memberships).findAllByOrganizationIdAndStatus(7L, MembershipStatus.ACTIVE);
    }

    @Test
    void listsNoMembersAsEmptyList() {
        when(memberships.findAllByOrganizationIdAndStatus(7L, MembershipStatus.ACTIVE)).thenReturn(List.of());

        assertTrue(service.listOrganizationMembers(7L).isEmpty());
    }

    @Test
    void acceptsPendingRequestByActivatingMemberAndReturnsActive() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        Membership membership = new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.PENDING);
        when(memberships.findByIdAndOrganizationId(11L, 7L)).thenReturn(java.util.Optional.of(membership));

        MembershipDecisionResponse response = service.decideMembershipRequest(7L, 11L,
                new MembershipDecisionRequest(MembershipDecision.ACCEPTED));

        assertEquals(MembershipStatus.ACTIVE, membership.getStatus());
        assertEquals(MembershipRole.MEMBER, membership.getRole());
        assertEquals(MembershipDecisionStatus.ACTIVE, response.getStatus());
        verify(memberships).save(membership);
        verify(memberships, never()).delete(any());
    }

    @Test
    void declinesPendingRequestByDeletingItAndReturnsDeclined() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        Membership membership = new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.PENDING);
        when(memberships.findByIdAndOrganizationId(11L, 7L)).thenReturn(java.util.Optional.of(membership));

        MembershipDecisionResponse response = service.decideMembershipRequest(7L, 11L,
                new MembershipDecisionRequest(MembershipDecision.DECLINED));

        assertEquals(MembershipDecisionStatus.DECLINED, response.getStatus());
        verify(memberships).delete(membership);
        verify(memberships, never()).save(any());
    }

    @Test
    void anAlreadyActiveRequestReturnsItsCurrentStatusWithoutChangingIt() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        Membership membership = new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.ACTIVE);
        when(memberships.findByIdAndOrganizationId(11L, 7L)).thenReturn(java.util.Optional.of(membership));

        MembershipDecisionResponse response = service.decideMembershipRequest(7L, 11L,
                new MembershipDecisionRequest(MembershipDecision.DECLINED));

        assertEquals(MembershipDecisionStatus.ACTIVE, response.getStatus());
        verify(memberships, never()).delete(any());
        verify(memberships, never()).save(any());
    }

    @Test
    void requestOutsideOrganizationIsNotFound() {
        when(memberships.findByIdAndOrganizationId(11L, 7L)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> service.decideMembershipRequest(7L, 11L,
                new MembershipDecisionRequest(MembershipDecision.ACCEPTED)));
    }

    @Test
    void promotesActiveMemberToManagerAndSavesMembership() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        Membership membership = new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.ACTIVE);
        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(java.util.Optional.of(membership));

        var response = service.promoteToManager(7L, 42L);

        assertEquals(42L, response.getUserId());
        assertEquals(MembershipRole.MANAGER, response.getRole());
        assertEquals(MembershipRole.MANAGER, membership.getRole());
        verify(memberships).save(membership);
    }

    @Test
    void rejectsMissingMembership() {
        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> service.promoteToManager(7L, 42L));
    }

    @Test
    void rejectsInactiveMembership() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        Membership membership = new Membership(11L, user, organization, MembershipRole.MEMBER, MembershipStatus.PENDING);
        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(java.util.Optional.of(membership));

        assertThrows(BusinessRuleException.class, () -> service.promoteToManager(7L, 42L));
        verify(memberships, never()).save(any());
    }

    @Test
    void rejectsAlreadyManager() {
        Organization organization = new Organization(7L, "Students", null, null);
        User user = new User(42L, "Ana", "Student", "ana@example.com", "password");
        Membership membership = new Membership(11L, user, organization, MembershipRole.MANAGER, MembershipStatus.ACTIVE);
        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(java.util.Optional.of(membership));

        assertThrows(ConflictException.class, () -> service.promoteToManager(7L, 42L));
        verify(memberships, never()).save(any());
    }
}
