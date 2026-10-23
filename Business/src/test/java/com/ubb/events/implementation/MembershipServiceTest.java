package com.ubb.events.implementation;

import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.domain.User;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.persistence.MembershipRepository;
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
    @InjectMocks MembershipServiceImpl service;

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
}
