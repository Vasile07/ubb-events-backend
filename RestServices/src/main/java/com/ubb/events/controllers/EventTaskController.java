package com.ubb.events.controllers;

import com.ubb.events.EventTaskService;
import com.ubb.events.dto.EventTaskResponse;
import com.ubb.events.security.RequireOrgPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
