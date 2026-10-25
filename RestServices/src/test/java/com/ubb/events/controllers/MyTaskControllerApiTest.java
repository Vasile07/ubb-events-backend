package com.ubb.events.controllers;

import com.ubb.events.EventTaskService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.EventStatus;
import com.ubb.events.dto.MyTaskEventResponse;
import com.ubb.events.dto.MyTaskOrganizationResponse;
import com.ubb.events.dto.MyTaskResponse;
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
class MyTaskControllerApiTest {
    private static final String MY_TASKS_URL = "/api/users/me/tasks";

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean EventTaskService eventTaskService;
    @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get(MY_TASKS_URL))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventTaskService);
    }

    @Test
    void returnsOnlyCurrentUsersTasksWithEventAndOrganization() throws Exception {
        when(eventTaskService.listMyTasks(42L)).thenReturn(List.of(
                new MyTaskResponse(10L, "Book the room",
                        new MyTaskEventResponse(1L, "Hackathon", LocalDate.of(2026, 11, 20), LocalTime.of(18, 0), EventStatus.PUBLISHED),
                        new MyTaskOrganizationResponse(7L, "Students"))));

        mockMvc.perform(get(MY_TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].description").value("Book the room"))
                .andExpect(jsonPath("$[0].event.id").value(1))
                .andExpect(jsonPath("$[0].event.title").value("Hackathon"))
                .andExpect(jsonPath("$[0].event.date").value("2026-11-20"))
                .andExpect(jsonPath("$[0].event.startTime").value("18:00:00"))
                .andExpect(jsonPath("$[0].event.status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].organization.id").value(7))
                .andExpect(jsonPath("$[0].organization.name").value("Students"))
                .andExpect(jsonPath("$[0].assignee").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
        verify(eventTaskService).listMyTasks(42L);
    }

    @Test
    void userWithoutTasksReceivesEmptyList() throws Exception {
        when(eventTaskService.listMyTasks(43L)).thenReturn(List.of());

        mockMvc.perform(get(MY_TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(43L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(eventTaskService).listMyTasks(43L);
    }

    @Test
    void doesNotRequireOrganizationMembership() throws Exception {
        when(eventTaskService.listMyTasks(42L)).thenReturn(List.of());

        mockMvc.perform(get(MY_TASKS_URL)
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk());
        verifyNoInteractions(authorizationService);
    }
}
