package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.EventService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerApiTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);
    private static final LocalTime START = LocalTime.of(18, 0);

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean EventService eventService;
    @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/organizations/7/events"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/organizations/7/events/1"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventService);
    }

    @Test
    void nonMemberIsForbidden() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(false);

        mockMvc.perform(get("/api/organizations/7/events")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(get("/api/organizations/7/events/1")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(authorizationService, org.mockito.Mockito.times(2)).hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER);
        verifyNoInteractions(eventService);
    }

    @Test
    void memberReceivesAllOrganizationEvents() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventService.listOrganizationEvents(7L)).thenReturn(List.of(
                new EventResponse(1L, 7L, 3L, "Hackathon", "Coding night",
                        DATE, START, LocalTime.of(22, 0), "Room 1", 50L, EventStatus.DRAFT),
                new EventResponse(2L, 7L, 3L, "Workshop", "Spring Boot",
                        DATE.plusDays(1), START, null, null, null, EventStatus.PUBLISHED),
                new EventResponse(3L, 7L, 4L, "Party", "Canceled party",
                        DATE.plusDays(2), START, null, null, null, EventStatus.CANCELED)));

        mockMvc.perform(get("/api/organizations/7/events")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].organizationId").value(7))
                .andExpect(jsonPath("$[0].categoryId").value(3))
                .andExpect(jsonPath("$[0].title").value("Hackathon"))
                .andExpect(jsonPath("$[0].description").value("Coding night"))
                .andExpect(jsonPath("$[0].date").value("2026-11-20"))
                .andExpect(jsonPath("$[0].startTime").value("18:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("22:00:00"))
                .andExpect(jsonPath("$[0].location").value("Room 1"))
                .andExpect(jsonPath("$[0].capacity").value(50))
                .andExpect(jsonPath("$[0].status").value("DRAFT"))
                .andExpect(jsonPath("$[0].tasks").doesNotExist())
                .andExpect(jsonPath("$[1].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[2].status").value("CANCELED"));
    }

    @Test
    void managerCanViewOrganizationEvents() throws Exception {
        // A manager satisfies the MEMBER-level check, which is the only level the interceptor asks for here.
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventService.listOrganizationEvents(7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/organizations/7/events")
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(authorizationService).hasAtLeast(43L, 7L, EffectiveOrgRole.MEMBER);
    }

    @Test
    void memberReceivesSingleOrganizationEvent() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventService.getOrganizationEvent(7L, 1L)).thenReturn(
                new EventResponse(1L, 7L, 3L, "Hackathon", "Coding night",
                        DATE, START, null, "Room 1", 50L, EventStatus.CANCELED));

        mockMvc.perform(get("/api/organizations/7/events/1")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.organizationId").value(7))
                .andExpect(jsonPath("$.title").value("Hackathon"))
                .andExpect(jsonPath("$.date").value("2026-11-20"))
                .andExpect(jsonPath("$.startTime").value("18:00:00"))
                .andExpect(jsonPath("$.endTime").doesNotExist())
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.tasks").doesNotExist());
    }

    @Test
    void unknownEventReturnsNotFound() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventService.getOrganizationEvent(7L, 99L)).thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get("/api/organizations/7/events/99")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Event not found"));
    }

    private static final String PUBLISH_URL = "/api/organizations/7/events/1/publish";

    @Test
    void unauthenticatedPublishIsRejected() throws Exception {
        mockMvc.perform(patch(PUBLISH_URL))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventService);
    }

    @Test
    void memberCannotPublish() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(false);

        mockMvc.perform(patch(PUBLISH_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(authorizationService).hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER);
        verifyNoInteractions(eventService);
    }

    @Test
    void managerPublishesDraft() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(eventService.publishEvent(7L, 1L)).thenReturn(
                new EventResponse(1L, 7L, 3L, "Hackathon", "Coding night",
                        DATE, START, LocalTime.of(22, 0), "Room 1", 50L, EventStatus.PUBLISHED));

        mockMvc.perform(patch(PUBLISH_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.endTime").value("22:00:00"));
        verify(eventService).publishEvent(7L, 1L);
    }

    @Test
    void pastEventReturnsBadRequestWithDateError() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(eventService.publishEvent(7L, 1L)).thenThrow(new BusinessRuleException("Event is not ready to be published", List.of(
                new ErrorDetail("date", "Event must start in the future"))));

        mockMvc.perform(patch(PUBLISH_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Event is not ready to be published"))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("date"))
                .andExpect(jsonPath("$.errors[0].message").value("Event must start in the future"));
    }

    @Test
    void alreadyPublishedOrCanceledReturnsConflict() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(eventService.publishEvent(7L, 1L)).thenThrow(new ConflictException("Event is already published"));

        mockMvc.perform(patch(PUBLISH_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Event is already published"));
    }

    @Test
    void unknownEventReturnsNotFoundOnPublish() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
        when(eventService.publishEvent(7L, 1L)).thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(patch(PUBLISH_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found"));
    }
}
