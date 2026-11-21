package com.ubb.events;

import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.MembershipRequestDetailsStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.dto.MembershipDecisionRequest;
import com.ubb.events.dto.MembershipDecisionResponse;
import com.ubb.events.dto.OrganizationMemberResponse;
import com.ubb.events.dto.RolePromotionResponse;
import com.ubb.events.dto.MembershipRequestDetailsResponse;
import java.util.List;

public interface MembershipService {
    void requestMembership(Long organizationId, Long userId);
    List<OrganizationMemberResponse> listOrganizationMembers(Long organizationId);
    List<MembershipRequestResponse> listMembershipRequests(Long organizationId, MembershipStatus status);
    List<MembershipRequestDetailsResponse> listMembershipRequestDetails(Long organizationId, MembershipRequestDetailsStatus status);
    MembershipDecisionResponse decideMembershipRequest(Long organizationId, Long requestId, MembershipDecisionRequest request);
    RolePromotionResponse promoteToManager(Long organizationId, Long userId);
}
