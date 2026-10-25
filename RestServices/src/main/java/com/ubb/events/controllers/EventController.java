package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.EventService;
import com.ubb.events.dto.EventResponse;
import com.ubb.events.dto.CreateEventRequest;
import com.ubb.events.security.RequireOrgPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{organizationId}/events")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "Create a draft event with tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Draft event created"),
            @ApiResponse(responseCode = "400", description = "Event details or task assignments are invalid"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Manager organization access required")
    })
    public ResponseEntity<EventResponse> createEvent(@PathVariable Long organizationId,
            @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(organizationId, request));
    }

    @GetMapping("/{organizationId}/events")
    @RequireOrgPermission
    @Operation(summary = "View organization events")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization events returned, including drafts and canceled events"),
            @ApiResponse(responseCode = "400", description = "Invalid organization id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required")
    })
    public ResponseEntity<List<EventResponse>> listOrganizationEvents(@PathVariable Long organizationId) {
        return ResponseEntity.ok(eventService.listOrganizationEvents(organizationId));
    }

    @GetMapping("/{organizationId}/events/{eventId}")
    @RequireOrgPermission
    @Operation(summary = "View an organization event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization event returned"),
            @ApiResponse(responseCode = "400", description = "Invalid organization or event id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> getOrganizationEvent(@PathVariable Long organizationId, @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getOrganizationEvent(organizationId, eventId));
    }

    @PatchMapping("/{organizationId}/events/{eventId}/publish")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "Publish a draft event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event published and returned"),
            @ApiResponse(responseCode = "400", description = "Event does not start in the future"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Manager organization access required"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event is already published or canceled")
    })
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long organizationId, @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.publishEvent(organizationId, eventId));
    }
}
