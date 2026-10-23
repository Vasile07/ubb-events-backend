package com.ubb.events;

import com.ubb.events.dto.OrganizationResponse;
import com.ubb.events.dto.OrganizationDetailsResponse;
import java.util.List;

public interface OrganizationService {
    List<OrganizationResponse> listOrganizations(Long userId);
    OrganizationDetailsResponse getOrganizationDetails(Long userId, Long organizationId);
}
