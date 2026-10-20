package com.ubb.events.dto;

import com.ubb.events.domain.MembershipRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MembershipResponse {
    private final Long organizationId;
    private final MembershipRole role;
}
