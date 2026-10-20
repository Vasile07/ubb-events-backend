package com.ubb.events;

public interface OrganizationAuthorizationService {
    EffectiveOrgRole effectiveRole(Long userId, Long organizationId);
    boolean hasAtLeast(Long userId, Long organizationId, EffectiveOrgRole minimumRole);
}
