package com.ubb.events.dto;

import com.ubb.events.domain.MembershipRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RolePromotionResponse {
    private final Long userId;
    private final MembershipRole role;
}
