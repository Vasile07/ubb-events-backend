package com.ubb.events.implementation;

import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.*;
import com.ubb.events.dto.UserProfileResponse;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ubb.events.common.security.JwtTokenService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final MembershipRepository memberships = mock(MembershipRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtTokenService tokens = mock(JwtTokenService.class);
    private final UserServiceImpl service = new UserServiceImpl(users, memberships, encoder, tokens);

    private final User user = new User(7L, "Ada", "Lovelace", "ada@example.com", "hash");
    private final Organization org1 = new Organization(11L, "CS", null, null);
    private final Organization org2 = new Organization(12L, "Math", null, null);

    @Test
    void mapsActualMembershipsAndFiltersPendingRequests() {
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(memberships.findAllByUserId(7L)).thenReturn(List.of(
                new Membership(1L, user, org1, MembershipRole.MEMBER, MembershipStatus.ACTIVE),
                new Membership(2L, user, org2, MembershipRole.MANAGER, MembershipStatus.ACTIVE),
                new Membership(3L, user, org1, MembershipRole.MANAGER, MembershipStatus.PENDING),
                new Membership(4L, user, org2, MembershipRole.MEMBER, MembershipStatus.PENDING)));

        UserProfileResponse response = service.getProfile(7L);

        assertEquals(7L, response.getId());
        assertEquals("Ada", response.getFirstName());
        assertEquals("ada@example.com", response.getEmail());
        assertEquals(3, response.getMemberships().size());
        assertEquals(MembershipRole.MEMBER, response.getMemberships().get(0).getRole());
        assertEquals(MembershipRole.MANAGER, response.getMemberships().get(1).getRole());
        assertEquals(MembershipRole.MEMBER, response.getMemberships().get(2).getRole());
        assertEquals(11L, response.getMemberships().get(2).getOrganizationId());
    }

    @Test
    void returnsEmptyMemberships() {
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(memberships.findAllByUserId(7L)).thenReturn(List.of());
        assertTrue(service.getProfile(7L).getMemberships().isEmpty());
    }

    @Test
    void missingUserIsNotFound() {
        when(users.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getProfile(9L));
        verifyNoInteractions(memberships);
    }
}
