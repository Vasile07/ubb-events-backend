package com.ubb.events.controllers;

import com.ubb.events.OrganizationService;
import com.ubb.events.dto.OrganizationResponse;
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
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @Operation(summary = "View organizations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizations returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> list(Principal principal) {
        Long userId = ((Number) ((org.springframework.security.core.Authentication) principal).getPrincipal()).longValue();
        return ResponseEntity.ok(organizationService.listOrganizations(userId));
    }
}
