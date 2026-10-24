package com.ubb.events.dto;

import com.ubb.events.domain.MembershipDecision;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MembershipDecisionRequest {
    @NotNull
    private MembershipDecision decision;
}
