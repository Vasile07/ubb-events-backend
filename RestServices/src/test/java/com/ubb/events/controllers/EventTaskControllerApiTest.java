package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.EventTaskService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.dto.TaskAssigneeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
class EventTaskControllerApiTest {
    private static final String TASKS_URL = "/api/organizations/7/events/1/tasks";

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean EventTaskService eventTaskService;
    @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get(TASKS_URL))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventTaskService);
    }

    @Test
    void nonMemberIsForbidden() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(false);

        mockMvc.perform(get(TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(authorizationService).hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER);
        verifyNoInteractions(eventTaskService);
    }

    @Test
    void memberReceivesEventTasksWithAssignees() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventTaskService.listEventTasks(7L, 1L)).thenReturn(List.of(
                new EventTaskResponse(10L, "Book the room", new TaskAssigneeResponse(99L, "Ana", "Student")),
                new EventTaskResponse(11L, "Print posters", null)));

        mockMvc.perform(get(TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].description").value("Book the room"))
                .andExpect(jsonPath("$[0].assignee.id").value(99))
                .andExpect(jsonPath("$[0].assignee.firstName").value("Ana"))
                .andExpect(jsonPath("$[0].assignee.lastName").value("Student"))
                .andExpect(jsonPath("$[0].assignee.email").doesNotExist())
                .andExpect(jsonPath("$[0].assignee.password").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].description").value("Print posters"))
                .andExpect(jsonPath("$[1].assignee").isEmpty());
    }

    @Test
    void managerCanViewEventTasks() throws Exception {
        // A manager satisfies the MEMBER-level check, which is the only level the interceptor asks for here.
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventTaskService.listEventTasks(7L, 1L)).thenReturn(List.of(
                new EventTaskResponse(10L, "Book the room", null)));

        mockMvc.perform(get(TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
        verify(authorizationService).hasAtLeast(43L, 7L, EffectiveOrgRole.MEMBER);
    }

    @Test
    void eventWithoutTasksReturnsEmptyList() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventTaskService.listEventTasks(7L, 1L)).thenReturn(List.of());

        mockMvc.perform(get(TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void eventOfAnotherOrganizationOrUnknownReturnsNotFound() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(eventTaskService.listEventTasks(7L, 1L)).thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get(TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Event not found"));
    }
}
