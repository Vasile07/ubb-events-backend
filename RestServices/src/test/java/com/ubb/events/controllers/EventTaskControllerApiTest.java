package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.EventTaskService;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.dto.ErrorDetail;
import com.ubb.events.dto.EventTaskRequest;
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private static final String TASK_URL = "/api/organizations/7/events/1/tasks/10";
    private static final String TASK_BODY = "{\"description\":\"Book the room\",\"assigneeUserId\":42}";
    private static final EventTaskRequest TASK_REQUEST = new EventTaskRequest("Book the room", 42L);

    private void manager() {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(true);
    }

    private String managerToken() {
        return "Bearer " + tokenService.issue(43L);
    }

    @Test
    void managerAddsTask() throws Exception {
        manager();
        when(eventTaskService.addTask(7L, 1L, TASK_REQUEST))
                .thenReturn(new EventTaskResponse(10L, "Book the room", new TaskAssigneeResponse(42L, "Ana", "Student")));

        mockMvc.perform(post(TASKS_URL).header("Authorization", managerToken())
                        .contentType("application/json").content(TASK_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.assignee.id").value(42));
        verify(eventTaskService).addTask(7L, 1L, TASK_REQUEST);
    }

    @Test
    void managerUpdatesTask() throws Exception {
        manager();
        when(eventTaskService.updateTask(7L, 1L, 10L, TASK_REQUEST))
                .thenReturn(new EventTaskResponse(10L, "Book the room", new TaskAssigneeResponse(42L, "Ana", "Student")));

        mockMvc.perform(put(TASK_URL).header("Authorization", managerToken())
                        .contentType("application/json").content(TASK_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Book the room"));
        verify(eventTaskService).updateTask(7L, 1L, 10L, TASK_REQUEST);
    }

    @Test
    void managerRemovesTask() throws Exception {
        manager();

        mockMvc.perform(delete(TASK_URL).header("Authorization", managerToken()))
                .andExpect(status().isNoContent());
        verify(eventTaskService).removeTask(7L, 1L, 10L);
    }

    @Test
    void memberCannotChangeTasksAndTokenIsRequired() throws Exception {
        mockMvc.perform(post(TASKS_URL).contentType("application/json").content(TASK_BODY))
                .andExpect(status().isUnauthorized());
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MANAGER)).thenReturn(false);
        String memberToken = "Bearer " + tokenService.issue(42L);
        mockMvc.perform(post(TASKS_URL).header("Authorization", memberToken).contentType("application/json").content(TASK_BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(TASK_URL).header("Authorization", memberToken).contentType("application/json").content(TASK_BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(TASK_URL).header("Authorization", memberToken))
                .andExpect(status().isForbidden());
        verifyNoInteractions(eventTaskService);
    }

    @Test
    void taskErrorsAreMapped() throws Exception {
        manager();
        when(eventTaskService.addTask(7L, 1L, TASK_REQUEST))
                .thenThrow(new BusinessRuleException("Task details are invalid",
                        List.of(new ErrorDetail("assigneeUserId", "Assignee must be an active member of the organization"))))
                .thenThrow(new ConflictException("Tasks of a canceled event cannot be changed"));
        doThrow(new NotFoundException("Task not found")).when(eventTaskService).removeTask(7L, 1L, 10L);

        mockMvc.perform(post(TASKS_URL).header("Authorization", managerToken()).contentType("application/json").content(TASK_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("assigneeUserId"));
        mockMvc.perform(post(TASKS_URL).header("Authorization", managerToken()).contentType("application/json").content(TASK_BODY))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(TASK_URL).header("Authorization", managerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }
}
