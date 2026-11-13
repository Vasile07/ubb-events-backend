package com.ubb.events.controllers;

import com.ubb.events.FollowUpService;
import com.ubb.events.dto.CreateFollowUpRequest;
import com.ubb.events.dto.FollowUpResponse;
import com.ubb.events.security.RequireOrgPermission;
import com.ubb.events.EffectiveOrgRole;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class FollowUpController {
    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @PostMapping("/{organizationId}/events/{eventId}/follow-up")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "Create a follow-up for a completed event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Follow-up created"),
            @ApiResponse(responseCode = "400", description = "Follow-up content is invalid"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization manager access required"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event is canceled or not completed")
    })
    public ResponseEntity<FollowUpResponse> create(@PathVariable Long organizationId, @PathVariable Long eventId,
            @Valid @RequestBody CreateFollowUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(followUpService.createFollowUp(organizationId, eventId, request));
    }

    @GetMapping("/{organizationId}/events/{eventId}/follow-up")
    @RequireOrgPermission
    @Operation(summary = "View an event follow-up")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Follow-up returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required"),
            @ApiResponse(responseCode = "404", description = "Follow-up not found")
    })
    public ResponseEntity<FollowUpResponse> get(@PathVariable Long organizationId, @PathVariable Long eventId) {
        return ResponseEntity.ok(followUpService.getFollowUp(organizationId, eventId));
    }
}
