package com.ubb.events.controllers;

import com.ubb.events.MembershipService;
import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.dto.MembershipDecisionRequest;
import com.ubb.events.dto.MembershipDecisionResponse;
import com.ubb.events.dto.OrganizationMemberResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

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

    @GetMapping("/{organizationId}/members")
    @RequireOrgPermission
    @Operation(summary = "View active organization members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active organization members returned"),
            @ApiResponse(responseCode = "400", description = "Invalid organization id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization membership required")
    })
    public ResponseEntity<List<OrganizationMemberResponse>> listOrganizationMembers(@PathVariable Long organizationId) {
        return ResponseEntity.ok(membershipService.listOrganizationMembers(organizationId));
    }

    @PatchMapping("/{organizationId}/membership-requests/{requestId}")
    @RequireOrgPermission(EffectiveOrgRole.MANAGER)
    @Operation(summary = "Decide an organization membership request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership request decision processed"),
            @ApiResponse(responseCode = "400", description = "Invalid decision or identifier"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Manager organization access required"),
            @ApiResponse(responseCode = "404", description = "Membership request not found"),
            @ApiResponse(responseCode = "409", description = "Membership request is not pending")
    })
    public ResponseEntity<MembershipDecisionResponse> decideMembershipRequest(
            @PathVariable Long organizationId, @PathVariable Long requestId,
            @Valid @RequestBody MembershipDecisionRequest request) {
        return ResponseEntity.ok(membershipService.decideMembershipRequest(organizationId, requestId, request));
    }
}
