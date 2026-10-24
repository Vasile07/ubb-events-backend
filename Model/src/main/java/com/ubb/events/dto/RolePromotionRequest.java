package com.ubb.events.dto;

import com.ubb.events.domain.MembershipRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RolePromotionRequest {
    @NotNull
    private MembershipRole role;
}
