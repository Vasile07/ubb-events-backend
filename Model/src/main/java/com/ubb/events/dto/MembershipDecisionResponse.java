package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MembershipDecisionResponse {
    private final MembershipDecisionStatus status;
}
