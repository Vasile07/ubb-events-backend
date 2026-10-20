package com.ubb.events.implementation;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.OrganizationRepository;
import com.ubb.events.common.errors.ForbiddenException;
import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import org.springframework.stereotype.Service;

@Service
public class OrganizationAuthorizationServiceImpl implements OrganizationAuthorizationService {
    private final MembershipRepository memberships;
    private final OrganizationRepository organizations;

    public OrganizationAuthorizationServiceImpl(MembershipRepository memberships, OrganizationRepository organizations) {
        this.memberships = memberships;
        this.organizations = organizations;
    }

    @Override
    public EffectiveOrgRole effectiveRole(Long userId, Long organizationId) {
        if (!organizations.existsById(organizationId)) {
            throw new ForbiddenException("Organization access is forbidden");
        }
        Membership membership = memberships.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new ForbiddenException("Organization access is forbidden"));
        MembershipRole role = membership.getRole();
        MembershipStatus status = membership.getStatus();
        if (role == null || status == null) return EffectiveOrgRole.NONE;
        if (status == MembershipStatus.ACTIVE) return toEffective(role);
        if (status == MembershipStatus.PENDING && role == MembershipRole.MANAGER) return EffectiveOrgRole.MEMBER;
        return EffectiveOrgRole.NONE;
    }

    @Override
    public boolean hasAtLeast(Long userId, Long organizationId, EffectiveOrgRole minimumRole) {
        return effectiveRole(userId, organizationId).atLeast(minimumRole);
    }

    private EffectiveOrgRole toEffective(MembershipRole role) {
        return role == MembershipRole.MANAGER ? EffectiveOrgRole.MANAGER : EffectiveOrgRole.MEMBER;
    }
}
