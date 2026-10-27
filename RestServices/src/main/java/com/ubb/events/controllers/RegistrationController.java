package com.ubb.events.controllers;

import com.ubb.events.RegistrationService;
import com.ubb.events.dto.RegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{eventId}/registrations")
    @Operation(summary = "Register the authenticated user for an event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Event or user not found"),
            @ApiResponse(responseCode = "409", description = "Event unavailable, full, or user already registered")
    })
    public ResponseEntity<RegistrationResponse> register(@PathVariable Long eventId, Authentication authentication) {
        Long userId = ((Number) authentication.getPrincipal()).longValue();
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.registerForEvent(eventId, userId));
    }
}
