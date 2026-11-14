package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.EventTaskService;
import com.ubb.events.dto.EventTaskRequest;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.security.RequireOrgPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class EventTaskController {
    private final EventTaskService eventTaskService;

    public EventTaskController(EventTaskService eventTaskService) {
        this.eventTaskService = eventTaskService;
    }

    @GetMapping("/{organizationId}/events/{eventId}/tasks")
    @RequireOrgPermission
    @Operation(summary = "View event tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event tasks returned with their assignees"),
            @ApiResponse(responseCode = "400", description = "Invalid organization or event id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<List<EventTaskResponse>> listEventTasks(@PathVariable Long organizationId, @PathVariable Long eventId) {
        return ResponseEntity.ok(eventTaskService.listEventTasks(organizationId, eventId));
    }

    @PostMapping("/{organizationId}/events/{eventId}/tasks")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "Add a task to an event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "400", description = "Description missing or assignee is not an active member"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Manager organization access required"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event is canceled or has already ended")
    })
    public ResponseEntity<EventTaskResponse> addTask(@PathVariable Long organizationId, @PathVariable Long eventId,
            @RequestBody EventTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventTaskService.addTask(organizationId, eventId, request));
    }

    @PutMapping("/{organizationId}/events/{eventId}/tasks/{taskId}")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "Edit or reassign an event task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated; a null assigneeUserId unassigns it"),
            @ApiResponse(responseCode = "400", description = "Description missing or assignee is not an active member"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Manager organization access required"),
            @ApiResponse(responseCode = "404", description = "Event or task not found"),
            @ApiResponse(responseCode = "409", description = "Event is canceled or has already ended")
    })
    public ResponseEntity<EventTaskResponse> updateTask(@PathVariable Long organizationId, @PathVariable Long eventId,
            @PathVariable Long taskId, @RequestBody EventTaskRequest request) {
        return ResponseEntity.ok(eventTaskService.updateTask(organizationId, eventId, taskId, request));
    }

    @DeleteMapping("/{organizationId}/events/{eventId}/tasks/{taskId}")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "Remove an event task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task removed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Manager organization access required"),
            @ApiResponse(responseCode = "404", description = "Event or task not found"),
            @ApiResponse(responseCode = "409", description = "Event is canceled or has already ended")
    })
    public ResponseEntity<Void> removeTask(@PathVariable Long organizationId, @PathVariable Long eventId,
            @PathVariable Long taskId) {
        eventTaskService.removeTask(organizationId, eventId, taskId);
        return ResponseEntity.noContent().build();
    }
}
