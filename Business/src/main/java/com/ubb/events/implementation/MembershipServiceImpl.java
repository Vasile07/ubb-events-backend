package com.ubb.events.implementation;

import com.ubb.events.MembershipService;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.persistence.MembershipRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MembershipServiceImpl implements MembershipService {
    private final MembershipRepository memberships;

    public MembershipServiceImpl(MembershipRepository memberships) {
        this.memberships = memberships;
    }

    @Override
    public List<MembershipRequestResponse> listMembershipRequests(Long organizationId, MembershipStatus status) {
        MembershipStatus requestedStatus = status == null ? MembershipStatus.PENDING : status;
        return memberships.findAllByOrganizationIdAndStatus(organizationId, requestedStatus).stream()
                .map(membership -> new MembershipRequestResponse(
                        membership.getId(),
                        membership.getUser().getId(),
                        membership.getRole(),
                        membership.getStatus()))
                .toList();
    }
}
