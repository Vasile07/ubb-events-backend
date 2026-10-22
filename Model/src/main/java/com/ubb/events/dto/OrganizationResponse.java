package com.ubb.events.dto;

public record OrganizationResponse(
        Long organizationId,
        String name,
        String description,
        OrganizationMembershipStatus status,
        OrganizationRole role) { }
