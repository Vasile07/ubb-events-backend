package com.ubb.events.dto;

import com.ubb.events.domain.MembershipRequestDetailsStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class MembershipRequestDetailsResponse {
    private final Long requestId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Instant requestedAt;
    private final MembershipRequestDetailsStatus status;
}
