package com.ubb.events.controllers;

import com.ubb.events.EventService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventDiscoveryFilter;
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
    private static final EventDiscoveryFilter NO_FILTER = new EventDiscoveryFilter(null, null, null, null);

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
    void withoutParametersUsesEmptyFilterAndReturnsSummaries() throws Exception {
        when(eventService.discoverEvents(NO_FILTER)).thenReturn(List.of(
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
        verify(eventService).discoverEvents(NO_FILTER);
    }

    @Test
    void bindsQueryParametersIntoFilter() throws Exception {
        EventDiscoveryFilter filter = new EventDiscoveryFilter("hack", 7L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        when(eventService.discoverEvents(filter)).thenReturn(List.of(
                new EventSummaryResponse(1L, "Hackathon", LocalDate.of(2026, 11, 20), LocalTime.of(18, 0),
                        null, null, EventStatus.PUBLISHED, new EventOrganizationSummaryResponse(7L, "Students"))));

        mockMvc.perform(get(DISCOVER_URL)
                        .param("search", "hack")
                        .param("organizationId", "7")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-30")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Hackathon"));
        verify(eventService).discoverEvents(filter);
    }

    @Test
    void noMatchesReturnsEmptyList() throws Exception {
        EventDiscoveryFilter filter = new EventDiscoveryFilter("nothing", null, null, null);
        when(eventService.discoverEvents(filter)).thenReturn(List.of());

        mockMvc.perform(get(DISCOVER_URL)
                        .param("search", "nothing")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(eventService).discoverEvents(filter);
    }

    @Test
    void invalidDateRangeReturnsBadRequest() throws Exception {
        EventDiscoveryFilter filter = new EventDiscoveryFilter(null, null, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1));
        when(eventService.discoverEvents(filter)).thenThrow(new BusinessRuleException("Invalid date range",
                List.of(new ErrorDetail("from", "From date must not be after the to date"))));

        mockMvc.perform(get(DISCOVER_URL)
                        .param("from", "2026-12-01")
                        .param("to", "2026-11-01")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid date range"))
                .andExpect(jsonPath("$.errors[0].field").value("from"));
    }

    @Test
    void malformedParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get(DISCOVER_URL)
                        .param("from", "not-a-date")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("from"));
        mockMvc.perform(get(DISCOVER_URL)
                        .param("organizationId", "abc")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("organizationId"));
        verifyNoInteractions(eventService);
    }

    @Test
    void doesNotRequireOrganizationMembership() throws Exception {
        when(eventService.discoverEvents(NO_FILTER)).thenReturn(List.of());

        mockMvc.perform(get(DISCOVER_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk());
        verifyNoInteractions(authorizationService);
    }
}
