package com.ubb.events.controllers;

import com.ubb.events.MembershipService;
import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.security.RequireOrgPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping("/{organizationId}/membership-requests")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "View organization membership requests")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership requests returned"),
            @ApiResponse(responseCode = "400", description = "Invalid organization id or status"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Manager organization access required")
    })
    public ResponseEntity<List<MembershipRequestResponse>> listMembershipRequests(
            @PathVariable Long organizationId,
            @RequestParam(required = false) MembershipStatus status) {
        return ResponseEntity.ok(membershipService.listMembershipRequests(organizationId, status));
    }
}
