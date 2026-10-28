package com.ubb.events.controllers;

import com.ubb.events.RegistrationService;
import com.ubb.events.dto.RegistrationDetailsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserRegistrationController {
    private final RegistrationService registrationService;

    public UserRegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/registrations")
    @Operation(summary = "View the authenticated user's registrations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active registrations returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required or token invalid")
    })
    public ResponseEntity<List<RegistrationDetailsResponse>> registrations(Principal principal) {
        Long userId = ((Number) ((org.springframework.security.core.Authentication) principal).getPrincipal()).longValue();
        return ResponseEntity.ok(registrationService.listMyRegistrations(userId));
    }
}
