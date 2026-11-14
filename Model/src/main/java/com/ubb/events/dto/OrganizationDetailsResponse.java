package com.ubb.events.dto;

public record OrganizationDetailsResponse(
        Long organizationId,
        String name,
        String description,
        String category,
        OrganizationMembershipStatus status) { }
