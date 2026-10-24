package com.ubb.events.dto;

import com.ubb.events.domain.MembershipRole;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class OrganizationMemberResponse {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final MembershipRole role;
}
