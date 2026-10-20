package com.ubb.events.implementation;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.OrganizationRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrganizationAuthorizationServiceImplTest {
    private final MembershipRepository memberships = mock(MembershipRepository.class);
    private final OrganizationRepository organizations = mock(OrganizationRepository.class);
    private final OrganizationAuthorizationServiceImpl service =
            new OrganizationAuthorizationServiceImpl(memberships, organizations);

    @Test
    void pendingManagerHasMemberAccessButNotManagerAccess() {
        when(organizations.existsById(10L)).thenReturn(true);
        when(memberships.findByUserIdAndOrganizationId(7L, 10L))
                .thenReturn(Optional.of(new com.ubb.events.domain.Membership(null, null, null,
                        MembershipRole.MANAGER, MembershipStatus.PENDING)));

        assertEquals(EffectiveOrgRole.MEMBER, service.effectiveRole(7L, 10L));
        assertEquals(true, service.hasAtLeast(7L, 10L, EffectiveOrgRole.MEMBER));
        assertEquals(false, service.hasAtLeast(7L, 10L, EffectiveOrgRole.MANAGER));
    }

    @Test
    void pendingMemberHasNoAccess() {
        when(organizations.existsById(10L)).thenReturn(true);
        when(memberships.findByUserIdAndOrganizationId(7L, 10L))
                .thenReturn(Optional.of(new com.ubb.events.domain.Membership(null, null, null,
                        MembershipRole.MEMBER, MembershipStatus.PENDING)));

        assertEquals(EffectiveOrgRole.NONE, service.effectiveRole(7L, 10L));
        assertEquals(false, service.hasAtLeast(7L, 10L, EffectiveOrgRole.MEMBER));
    }

    @Test
    void activeManagerKeepsManagerRole() {
        when(organizations.existsById(10L)).thenReturn(true);
        when(memberships.findByUserIdAndOrganizationId(7L, 10L))
                .thenReturn(Optional.of(new com.ubb.events.domain.Membership(null, null, null,
                        MembershipRole.MANAGER, MembershipStatus.ACTIVE)));

        assertEquals(EffectiveOrgRole.MANAGER, service.effectiveRole(7L, 10L));
    }

    @Test
    void unknownOrganizationAndNonMemberAreForbidden() {
        when(organizations.existsById(10L)).thenReturn(false);
        assertThrows(com.ubb.events.common.errors.ForbiddenException.class,
                () -> service.effectiveRole(7L, 10L));

        when(organizations.existsById(11L)).thenReturn(true);
        when(memberships.findByUserIdAndOrganizationId(7L, 11L)).thenReturn(Optional.empty());
        assertThrows(com.ubb.events.common.errors.ForbiddenException.class,
                () -> service.effectiveRole(7L, 11L));
    }

    @Test
    void membershipInOrganizationADoesNotAuthorizeOrganizationB() {
        when(organizations.existsById(1L)).thenReturn(true);
        when(organizations.existsById(2L)).thenReturn(true);
        when(memberships.findByUserIdAndOrganizationId(7L, 1L))
                .thenReturn(Optional.of(new com.ubb.events.domain.Membership(null, null, null,
                        MembershipRole.MEMBER, MembershipStatus.ACTIVE)));
        when(memberships.findByUserIdAndOrganizationId(7L, 2L)).thenReturn(Optional.empty());

        assertEquals(EffectiveOrgRole.MEMBER, service.effectiveRole(7L, 1L));
        assertThrows(com.ubb.events.common.errors.ForbiddenException.class,
                () -> service.effectiveRole(7L, 2L));
    }
}
