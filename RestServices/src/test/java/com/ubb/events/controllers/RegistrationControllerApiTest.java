package com.ubb.events.controllers;

import com.ubb.events.RegistrationService;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationControllerApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean RegistrationService registrationService;

    @Test
    void unauthenticatedCancellationIsRejected() throws Exception {
        mockMvc.perform(delete("/api/events/7/registrations/me"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(registrationService);
    }

    @Test
    void authenticatedCancellationReturnsNoContentAndPropagatesUser() throws Exception {
        mockMvc.perform(delete("/api/events/7/registrations/me")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(registrationService).cancelRegistration(7L, 42L);
    }

    @Test
    void missingRegistrationUsesSharedNotFoundResponse() throws Exception {
        doThrow(new NotFoundException("Active registration not found"))
                .when(registrationService).cancelRegistration(7L, 42L);

        mockMvc.perform(delete("/api/events/7/registrations/me")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Active registration not found"));
    }

    @Test
    void startedEventUsesSharedConflictResponse() throws Exception {
        doThrow(new ConflictException("Registration cannot be canceled because the event has started"))
                .when(registrationService).cancelRegistration(7L, 42L);

        mockMvc.perform(delete("/api/events/7/registrations/me")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Registration cannot be canceled because the event has started"));
    }
}
