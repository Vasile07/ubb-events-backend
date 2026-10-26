package com.ubb.events.implementation;

import com.ubb.events.MembershipService;
import com.ubb.events.common.errors.BusinessRuleException;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipDecision;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.dto.MembershipDecisionRequest;
import com.ubb.events.dto.MembershipDecisionResponse;
import com.ubb.events.dto.MembershipDecisionStatus;
import com.ubb.events.dto.OrganizationMemberResponse;
import com.ubb.events.dto.RolePromotionResponse;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.OrganizationRepository;
import com.ubb.events.persistence.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipServiceImpl implements MembershipService {
    private final MembershipRepository memberships;
    private final OrganizationRepository organizations;
    private final UserRepository users;

    public MembershipServiceImpl(MembershipRepository memberships, OrganizationRepository organizations, UserRepository users) {
        this.memberships = memberships;
        this.organizations = organizations;
        this.users = users;
    }

    @Override
    @Transactional
    public void requestMembership(Long organizationId, Long userId) {
        var organization = organizations.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
        var user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        var existing = memberships.findByUserIdAndOrganizationId(userId, organizationId);
        if (existing.isPresent()) {
            Membership membership = existing.get();
            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                throw new BusinessRuleException("You are already a member of this organization");
            }
            if (membership.getStatus() == MembershipStatus.PENDING) {
                throw new ConflictException("A membership request is already pending");
            }
        }
        memberships.save(new Membership(null, user, organization,
                MembershipRole.MEMBER, MembershipStatus.PENDING));
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

    @Override
    public List<OrganizationMemberResponse> listOrganizationMembers(Long organizationId) {
        return memberships.findAllByOrganizationIdAndStatus(organizationId, MembershipStatus.ACTIVE).stream()
                .map(membership -> new OrganizationMemberResponse(
                        membership.getUser().getId(),
                        membership.getUser().getFirstName(),
                        membership.getUser().getLastName(),
                        membership.getUser().getEmail(),
                        membership.getRole()))
                .toList();
    }

    @Override
    @Transactional
    public MembershipDecisionResponse decideMembershipRequest(Long organizationId, Long requestId, MembershipDecisionRequest request) {
        Membership membership = memberships.findByIdAndOrganizationId(requestId, organizationId)
                .orElseThrow(() -> new NotFoundException("Membership request not found"));

        if (membership.getStatus() == MembershipStatus.ACTIVE) {
            return new MembershipDecisionResponse(MembershipDecisionStatus.ACTIVE);
        }
        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new ConflictException("Only pending membership requests can be decided");
        }

        if (request.getDecision() == MembershipDecision.ACCEPTED) {
            membership.setRole(MembershipRole.MEMBER);
            membership.setStatus(MembershipStatus.ACTIVE);
            memberships.save(membership);
            return new MembershipDecisionResponse(MembershipDecisionStatus.ACTIVE);
        }

        memberships.delete(membership);
        return new MembershipDecisionResponse(MembershipDecisionStatus.DECLINED);
    }

    @Override
    @Transactional
    public RolePromotionResponse promoteToManager(Long organizationId, Long userId) {
        Membership membership = memberships.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new NotFoundException("Organization member not found"));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new BusinessRuleException("Only active organization members can be promoted");
        }
        if (membership.getRole() == MembershipRole.MANAGER) {
            throw new ConflictException("Organization member is already a manager");
        }
        if (membership.getRole() != MembershipRole.MEMBER) {
            throw new BusinessRuleException("Only organization members can be promoted");
        }

        membership.setRole(MembershipRole.MANAGER);
        memberships.save(membership);
        return new RolePromotionResponse(userId, MembershipRole.MANAGER);
    }
}
