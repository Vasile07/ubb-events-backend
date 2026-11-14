package com.ubb.events.implementation;

import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.dto.OrganizationResponse;
import com.ubb.events.dto.OrganizationDetailsResponse;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.OrganizationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationServiceTest {
    private final OrganizationRepository organizations = mock(OrganizationRepository.class);
    private final MembershipRepository memberships = mock(MembershipRepository.class);
    private final OrganizationServiceImpl service = new OrganizationServiceImpl(organizations, memberships);

    @Test
    void listsPublicOrganizationsAndMapsMembershipStatesToEffectiveRoles() {
        Organization none = new Organization(1L, "None", "public", "private-logo");
        Organization member = new Organization(2L, "Member", null, null);
        Organization activeMember = new Organization(6L, "Active member", "details", null);
        Organization pendingManager = new Organization(3L, "Pending manager", "description", null);
        Organization pendingMember = new Organization(4L, "Pending member", null, null);
        Organization declined = new Organization(5L, "Declined", null, null);
        when(organizations.findAll()).thenReturn(List.of(none, member, pendingManager, pendingMember, declined, activeMember));
        when(memberships.findAllByUserId(9L)).thenReturn(List.of(
                new Membership(1L, null, member, MembershipRole.MANAGER, MembershipStatus.ACTIVE),
                new Membership(2L, null, pendingManager, MembershipRole.MANAGER, MembershipStatus.PENDING),
                new Membership(3L, null, pendingMember, MembershipRole.MEMBER, MembershipStatus.PENDING),
                new Membership(4L, null, declined, null, null),
                new Membership(5L, null, activeMember, MembershipRole.MEMBER, MembershipStatus.ACTIVE)));

        List<OrganizationResponse> result = service.listOrganizations(9L);

        assertEquals(6, result.size());
        assertEquals("NONE", result.get(0).status().name());
        assertEquals("NONE", result.get(0).role().name());
        assertEquals("MANAGER", result.get(1).role().name());
        assertEquals("PENDING", result.get(2).status().name());
        assertEquals("MEMBER", result.get(2).role().name());
        assertEquals("PENDING", result.get(3).status().name());
        assertEquals("NONE", result.get(3).role().name());
        assertEquals("NONE", result.get(4).status().name());
        assertEquals("NONE", result.get(4).role().name());
        assertEquals(6L, result.get(5).organizationId());
        assertEquals("Active member", result.get(5).name());
        assertEquals("details", result.get(5).description());
        assertEquals("MEMBER", result.get(5).status().name());
        assertEquals("MEMBER", result.get(5).role().name());
    }

    @Test
    void getsDetailsAndMapsMembershipStatusWithoutPrivateFields() {
        Organization organization = new Organization(7L, "Students", "Public description", "private-logo");
        when(organizations.findById(7L)).thenReturn(Optional.of(organization));
        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(
                Optional.of(new Membership(1L, null, organization, MembershipRole.MANAGER, MembershipStatus.ACTIVE)));

        OrganizationDetailsResponse result = service.getOrganizationDetails(42L, 7L);

        assertEquals(new OrganizationDetailsResponse(7L, "Students", "Public description",
                com.ubb.events.dto.OrganizationMembershipStatus.MEMBER), result);
    }

    @Test
    void detailsMapMissingAndDeclinedMembershipToNoneAndPendingToPending() {
        Organization organization = new Organization(7L, "Students", "Public description", null);
        when(organizations.findById(7L)).thenReturn(Optional.of(organization));
        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(Optional.empty());
        assertEquals("NONE", service.getOrganizationDetails(42L, 7L).status().name());

        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(
                Optional.of(new Membership(1L, null, organization, null, null)));
        assertEquals("NONE", service.getOrganizationDetails(42L, 7L).status().name());

        when(memberships.findByUserIdAndOrganizationId(42L, 7L)).thenReturn(
                Optional.of(new Membership(1L, null, organization, MembershipRole.MEMBER, MembershipStatus.PENDING)));
        assertEquals("PENDING", service.getOrganizationDetails(42L, 7L).status().name());
    }

    @Test
    void missingOrganizationIsNotFound() {
        when(organizations.findById(99L)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class,
                () -> service.getOrganizationDetails(42L, 99L));
    }
}
