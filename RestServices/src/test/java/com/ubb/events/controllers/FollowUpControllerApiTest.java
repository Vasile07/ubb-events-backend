package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.FollowUpService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.dto.FollowUpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FollowUpControllerApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean FollowUpService followUpService;
    @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/organizations/7/events/3/follow-up")
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Feedback\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(followUpService);
    }

    @Test
    void managerCreatesFollowUp() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(followUpService.createFollowUp(eq(7L), eq(3L), any())).thenReturn(new FollowUpResponse(9L, 3L, "Feedback"));
        mockMvc.perform(post("/api/organizations/7/events/3/follow-up")
                .header("Authorization", "Bearer " + tokenService.issue(43L))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Feedback\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void memberCannotCreateFollowUp() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(false);
        mockMvc.perform(post("/api/organizations/7/events/3/follow-up")
                .header("Authorization", "Bearer " + tokenService.issue(42L))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Feedback\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(followUpService);
    }

    @Test
    void blankContentReturnsValidationError() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        mockMvc.perform(post("/api/organizations/7/events/3/follow-up")
                .header("Authorization", "Bearer " + tokenService.issue(43L))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(followUpService);
    }
}
