package com.ubb.events.controllers;

import com.ubb.events.AnnouncementService;
import com.ubb.events.dto.AnnouncementResponse;
import com.ubb.events.dto.CreateAnnouncementRequest;
import com.ubb.events.security.RequireOrgPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api")
public class AnnouncementController {
    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping("/organizations/{organizationId}/events/{eventId}/announcements")
    @RequireOrgPermission
    @Operation(summary = "Publish an event announcement")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Announcement created"),
            @ApiResponse(responseCode = "400", description = "Announcement title or content is invalid"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event is canceled")
    })
    public ResponseEntity<AnnouncementResponse> create(@PathVariable Long organizationId, @PathVariable Long eventId,
            @Valid @RequestBody CreateAnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.create(organizationId, eventId, request));
    }

    @GetMapping("/events/{eventId}/announcements")
    @Operation(summary = "View event announcements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Announcements returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Event announcement access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<java.util.List<AnnouncementResponse>> list(@PathVariable Long eventId, Authentication authentication) {
        Long userId = ((Number) authentication.getPrincipal()).longValue();
        return ResponseEntity.ok(announcementService.list(eventId, userId));
    }
}
