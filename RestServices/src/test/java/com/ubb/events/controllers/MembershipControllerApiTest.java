package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.MembershipService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MembershipControllerApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean MembershipService membershipService;
    @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/organizations/7/membership-requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void managerReceivesPendingRequestsByDefault() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(membershipService.listMembershipRequests(7L, null)).thenReturn(List.of(
                new MembershipRequestResponse(11L, 99L, MembershipRole.MEMBER, MembershipStatus.PENDING)));

        mockMvc.perform(get("/api/organizations/7/membership-requests")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].membershipId").value(11))
                .andExpect(jsonPath("$[0].userId").value(99))
                .andExpect(jsonPath("$[0].role").value("MEMBER"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void nonManagerIsForbidden() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(false);

        mockMvc.perform(get("/api/organizations/7/membership-requests")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidStatusIsBadRequest() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);

        mockMvc.perform(get("/api/organizations/7/membership-requests?status=UNKNOWN")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("status"));
    }
}
