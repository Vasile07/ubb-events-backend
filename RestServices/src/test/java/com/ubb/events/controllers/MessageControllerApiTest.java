package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.MessageService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.dto.MessageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageControllerApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean MessageService messageService;
    @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test
    void memberCanViewDraftEventMessages() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(messageService.listEventMessages(7L, 3L)).thenReturn(List.of(new MessageResponse(5L, 42L, "Hello")));

        mockMvc.perform(get("/api/organizations/7/events/3/messages")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].senderId").value(42))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    void managerCanViewEmptyMessages() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(messageService.listEventMessages(7L, 3L)).thenReturn(List.of());

        mockMvc.perform(get("/api/organizations/7/events/3/messages")
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void nonMemberIsForbidden() throws Exception {
        when(authorizationService.hasAtLeast(44L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(false);
        mockMvc.perform(get("/api/organizations/7/events/3/messages")
                        .header("Authorization", "Bearer " + tokenService.issue(44L)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(messageService);
    }

    @Test
    void unauthenticatedIsRejected() throws Exception {
        mockMvc.perform(get("/api/organizations/7/events/3/messages"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(messageService);
    }
}
