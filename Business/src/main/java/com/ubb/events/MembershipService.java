package com.ubb.events;

import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.dto.MembershipDecisionRequest;
import com.ubb.events.dto.MembershipDecisionResponse;
import java.util.List;

public interface MembershipService {
    List<MembershipRequestResponse> listMembershipRequests(Long organizationId, MembershipStatus status);
    MembershipDecisionResponse decideMembershipRequest(Long organizationId, Long requestId, MembershipDecisionRequest request);
}
