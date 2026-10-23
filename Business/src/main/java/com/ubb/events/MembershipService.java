package com.ubb.events;

import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import java.util.List;

public interface MembershipService {
    List<MembershipRequestResponse> listMembershipRequests(Long organizationId, MembershipStatus status);
}
