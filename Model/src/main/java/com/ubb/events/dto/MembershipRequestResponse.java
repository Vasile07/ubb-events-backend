package com.ubb.events.dto;

import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class MembershipRequestResponse {
    private final Long membershipId;
    private final Long userId;
    private final MembershipRole role;
    private final MembershipStatus status;
}
