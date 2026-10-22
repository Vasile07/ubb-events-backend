package com.ubb.events.implementation;

import com.ubb.events.OrganizationService;
import com.ubb.events.dto.OrganizationResponse;
import com.ubb.events.dto.OrganizationMembershipStatus;
import com.ubb.events.dto.OrganizationRole;
import com.ubb.events.domain.Membership;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.Organization;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.persistence.OrganizationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;

    public OrganizationServiceImpl(OrganizationRepository organizations, MembershipRepository memberships) {
        this.organizations = organizations;
        this.memberships = memberships;
    }

    @Override
    public List<OrganizationResponse> listOrganizations(Long userId) {
        var byOrganization = memberships.findAllByUserId(userId).stream()
                .collect(java.util.stream.Collectors.toMap(
                        membership -> membership.getOrganization().getId(),
                        membership -> membership,
                        (first, ignored) -> first));
        return organizations.findAll().stream()
                .map(organization -> toResponse(organization, byOrganization.get(organization.getId())))
                .toList();
    }

    private OrganizationResponse toResponse(Organization organization, Membership membership) {
        OrganizationMembershipStatus status = OrganizationMembershipStatus.NONE;
        OrganizationRole role = OrganizationRole.NONE;
        if (membership != null && membership.getStatus() != null && membership.getRole() != null) {
            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                status = OrganizationMembershipStatus.MEMBER;
                role = membership.getRole() == MembershipRole.MANAGER
                        ? OrganizationRole.MANAGER : OrganizationRole.MEMBER;
            } else if (membership.getStatus() == MembershipStatus.PENDING) {
                status = OrganizationMembershipStatus.PENDING;
                role = membership.getRole() == MembershipRole.MANAGER
                        ? OrganizationRole.MEMBER : OrganizationRole.NONE;
            }
        }
        return new OrganizationResponse(organization.getId(), organization.getName(), organization.getDescription(), status, role);
    }
}
