package com.ubb.events.controllers;

import com.ubb.events.OrganizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.dto.OrganizationMembershipStatus;
import com.ubb.events.dto.OrganizationResponse;
import com.ubb.events.dto.OrganizationRole;
import com.ubb.events.dto.OrganizationDetailsResponse;
import com.ubb.events.implementation.OrganizationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganizationControllerApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;
    @MockitoBean OrganizationServiceImpl organizationService;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedListReturnsOnlyPublicOrganizationFieldsAndComputedState() throws Exception {
        when(organizationService.listOrganizations(42L)).thenReturn(List.of(
                new OrganizationResponse(7L, "Students", "Public description",
                        OrganizationMembershipStatus.MEMBER, OrganizationRole.MANAGER)));

        mockMvc.perform(get("/api/organizations")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].organizationId").value(7))
                .andExpect(jsonPath("$[0].name").value("Students"))
                .andExpect(jsonPath("$[0].description").value("Public description"))
                .andExpect(jsonPath("$[0].status").value("MEMBER"))
                .andExpect(jsonPath("$[0].role").value("MANAGER"))
                .andExpect(jsonPath("$[0].logoUrl").doesNotExist())
                .andExpect(jsonPath("$[0].members").doesNotExist())
                .andExpect(jsonPath("$[0].requests").doesNotExist());
    }

    @Test
    void detailsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/organizations/7"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedDetailsReturnsPublicFieldsAndStatusOnly() throws Exception {
        when(organizationService.getOrganizationDetails(42L, 7L)).thenReturn(
                new OrganizationDetailsResponse(7L, "Students", "Public description",
                        OrganizationMembershipStatus.MEMBER));

        mockMvc.perform(get("/api/organizations/7")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(7))
                .andExpect(jsonPath("$.name").value("Students"))
                .andExpect(jsonPath("$.description").value("Public description"))
                .andExpect(jsonPath("$.status").value("MEMBER"))
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.logoUrl").doesNotExist())
                .andExpect(jsonPath("$.members").doesNotExist())
                .andExpect(jsonPath("$.requests").doesNotExist());
    }

    @Test
    void authenticatedDetailsMapsMissingOrganizationToNotFound() throws Exception {
        when(organizationService.getOrganizationDetails(42L, 99L))
                .thenThrow(new com.ubb.events.common.errors.NotFoundException("Organization not found"));

        mockMvc.perform(get("/api/organizations/99")
                        .header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
