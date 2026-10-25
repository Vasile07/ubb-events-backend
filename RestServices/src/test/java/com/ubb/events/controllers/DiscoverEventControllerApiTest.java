package com.ubb.events.controllers;

import com.ubb.events.EventService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.dto.EventOrganizationSummaryResponse;
import com.ubb.events.dto.EventSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiscoverEventControllerApiTest {
    private static final String DISCOVER_URL = "/api/events";

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean EventService eventService;
    @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get(DISCOVER_URL))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventService);
    }

    @Test
    void signedInUserDiscoversPublishedEvents() throws Exception {
        when(eventService.discoverEvents()).thenReturn(List.of(
                new EventSummaryResponse(1L, "Hackathon", LocalDate.of(2026, 11, 20), LocalTime.of(18, 0),
                        LocalTime.of(22, 0), "Room 1", EventStatus.PUBLISHED,
                        new EventOrganizationSummaryResponse(7L, "Students")),
                new EventSummaryResponse(2L, "Quiz", LocalDate.of(2026, 11, 21), LocalTime.of(18, 0),
                        null, null, EventStatus.PUBLISHED,
                        new EventOrganizationSummaryResponse(8L, "Math club"))));

        mockMvc.perform(get(DISCOVER_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Hackathon"))
                .andExpect(jsonPath("$[0].date").value("2026-11-20"))
                .andExpect(jsonPath("$[0].startTime").value("18:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("22:00:00"))
                .andExpect(jsonPath("$[0].location").value("Room 1"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].organization.id").value(7))
                .andExpect(jsonPath("$[0].organization.name").value("Students"))
                .andExpect(jsonPath("$[0].description").doesNotExist())
                .andExpect(jsonPath("$[0].capacity").doesNotExist())
                .andExpect(jsonPath("$[0].tasks").doesNotExist())
                .andExpect(jsonPath("$[1].organization.name").value("Math club"));
        verify(eventService).discoverEvents();
    }

    @Test
    void noResultsReturnsEmptyList() throws Exception {
        when(eventService.discoverEvents()).thenReturn(List.of());

        mockMvc.perform(get(DISCOVER_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(eventService).discoverEvents();
    }

    @Test
    void doesNotRequireOrganizationMembership() throws Exception {
        when(eventService.discoverEvents()).thenReturn(List.of());

        mockMvc.perform(get(DISCOVER_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk());
        verifyNoInteractions(authorizationService);
    }
}
