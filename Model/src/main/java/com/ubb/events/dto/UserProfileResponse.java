package com.ubb.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final List<MembershipResponse> memberships;
}
