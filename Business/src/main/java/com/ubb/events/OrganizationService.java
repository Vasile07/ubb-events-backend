package com.ubb.events;

import com.ubb.events.dto.OrganizationResponse;
import java.util.List;

public interface OrganizationService {
    List<OrganizationResponse> listOrganizations(Long userId);
}
