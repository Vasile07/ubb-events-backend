package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.MembershipService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.dto.MembershipDecisionRequest;
import com.ubb.events.dto.MembershipDecisionResponse;
import com.ubb.events.dto.MembershipDecisionStatus;
import com.ubb.events.dto.OrganizationMemberResponse;
import com.ubb.events.dto.RolePromotionResponse;
import com.ubb.events.domain.MembershipDecision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    void authenticatedNonMemberCanSubmitEmptyMembershipRequest() throws Exception {
        mockMvc.perform(post("/api/organizations/7/membership-requests")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void unauthenticatedPostRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/organizations/7/membership-requests"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void malformedOrganizationIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/organizations/not-a-number/membership-requests")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("organizationId"));
    }

    @Test
    void duplicatePendingRequestMapsToConflictError() throws Exception {
        doThrow(new com.ubb.events.common.errors.ConflictException("A membership request is already pending"))
                .when(membershipService).requestMembership(7L, 42L);

        mockMvc.perform(post("/api/organizations/7/membership-requests")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A membership request is already pending"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void activeMemberMapsToBusinessRuleError() throws Exception {
        doThrow(new com.ubb.events.common.errors.BusinessRuleException("You are already a member of this organization"))
                .when(membershipService).requestMembership(7L, 42L);

        mockMvc.perform(post("/api/organizations/7/membership-requests")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("You are already a member of this organization"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void unknownOrganizationMapsToNotFoundError() throws Exception {
        doThrow(new com.ubb.events.common.errors.NotFoundException("Organization not found"))
                .when(membershipService).requestMembership(7L, 42L);

        mockMvc.perform(post("/api/organizations/7/membership-requests")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Organization not found"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/organizations/7/membership-requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void activeMemberReceivesOrganizationMembersWithPersonalFieldsAndRole() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(membershipService.listOrganizationMembers(7L)).thenReturn(List.of(
                new OrganizationMemberResponse(99L, "Ana", "Student", "ana@example.com", MembershipRole.MEMBER)));

        mockMvc.perform(get("/api/organizations/7/members")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99))
                .andExpect(jsonPath("$[0].firstName").value("Ana"))
                .andExpect(jsonPath("$[0].lastName").value("Student"))
                .andExpect(jsonPath("$[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$[0].role").value("MEMBER"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void membersEndpointRequiresActiveOrganizationMembership() throws Exception {
        mockMvc.perform(get("/api/organizations/7/members"))
                .andExpect(status().isUnauthorized());
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(false);
        mockMvc.perform(get("/api/organizations/7/members")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isForbidden());
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

    @Test
    void managerCanAcceptRequestAndReceivesActiveStatus() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(membershipService.decideMembershipRequest(7L, 11L,
                new MembershipDecisionRequest(MembershipDecision.ACCEPTED)))
                .thenReturn(new MembershipDecisionResponse(MembershipDecisionStatus.ACTIVE));

        mockMvc.perform(patch("/api/organizations/7/membership-requests/11")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json")
                        .content("{\"decision\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void managerCanDeclineRequestAndReceivesDeclinedOutcome() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(membershipService.decideMembershipRequest(7L, 11L,
                new MembershipDecisionRequest(MembershipDecision.DECLINED)))
                .thenReturn(new MembershipDecisionResponse(MembershipDecisionStatus.DECLINED));

        mockMvc.perform(patch("/api/organizations/7/membership-requests/11")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json")
                        .content("{\"decision\":\"DECLINED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    void patchDecisionRequiresAuthenticationAndManagerAuthorization() throws Exception {
        mockMvc.perform(patch("/api/organizations/7/membership-requests/11")
                        .contentType("application/json").content("{\"decision\":\"ACCEPTED\"}"))
                .andExpect(status().isUnauthorized());
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(false);
        mockMvc.perform(patch("/api/organizations/7/membership-requests/11")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{\"decision\":\"ACCEPTED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchDecisionRejectsInvalidOrMissingDecision() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        mockMvc.perform(patch("/api/organizations/7/membership-requests/11")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{\"decision\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/organizations/7/membership-requests/11")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void managerCanPromoteActiveMember() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(membershipService.promoteToManager(7L, 99L))
                .thenReturn(new RolePromotionResponse(99L, MembershipRole.MANAGER));

        mockMvc.perform(patch("/api/organizations/7/members/99/role")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json")
                        .content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(99))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void rolePromotionRequiresAuthenticationAndManagerAuthorization() throws Exception {
        mockMvc.perform(patch("/api/organizations/7/members/99/role")
                        .contentType("application/json").content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isUnauthorized());
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(false);
        mockMvc.perform(patch("/api/organizations/7/members/99/role")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rolePromotionRejectsInvalidRoleBody() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        mockMvc.perform(patch("/api/organizations/7/members/99/role")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{\"role\":\"MEMBER\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/organizations/7/members/99/role")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rolePromotionMapsMissingMemberToNotFound() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(membershipService.promoteToManager(7L, 99L))
                .thenThrow(new com.ubb.events.common.errors.NotFoundException("Organization member not found"));

        mockMvc.perform(patch("/api/organizations/7/members/99/role")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Organization member not found"));
    }

    @Test
    void rolePromotionMapsAlreadyManagerToConflict() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(membershipService.promoteToManager(7L, 99L))
                .thenThrow(new com.ubb.events.common.errors.ConflictException("Organization member is already a manager"));

        mockMvc.perform(patch("/api/organizations/7/members/99/role")
                        .header("Authorization", "Bearer " + tokenService.issue(42L))
                        .contentType("application/json").content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Organization member is already a manager"));
    }
}
