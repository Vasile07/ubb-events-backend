package com.ubb.events.controllers;

import com.ubb.events.AnnouncementService;
import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.OrganizationAuthorizationService;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.dto.AnnouncementResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AnnouncementControllerApiTest {
    @Autowired MockMvc mockMvc; @Autowired JwtTokenService tokenService;
    @MockitoBean AnnouncementService announcementService; @MockitoBean OrganizationAuthorizationService authorizationService;

    @Test void registeredParticipantCanViewAnnouncements() throws Exception {
        when(announcementService.list(3L, 42L)).thenReturn(java.util.List.of(new AnnouncementResponse(9L, 3L, "Title", "Content")));
        mockMvc.perform(get("/api/events/3/announcements").header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(9)).andExpect(jsonPath("$[0].eventId").value(3))
                .andExpect(jsonPath("$[0].title").value("Title")).andExpect(jsonPath("$[0].content").value("Content"))
                .andExpect(jsonPath("$[0].author").doesNotExist()).andExpect(jsonPath("$[0].creationDate").doesNotExist());
    }

    @Test void unauthenticatedCannotViewAnnouncements() throws Exception {
        mockMvc.perform(get("/api/events/3/announcements")).andExpect(status().isUnauthorized());
        verifyNoInteractions(announcementService);
    }

    @Test void emptyAnnouncementsAreReturnedAsEmptyArray() throws Exception {
        when(announcementService.list(3L, 42L)).thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/events/3/announcements").header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void unknownEventIsReturnedAsNotFound() throws Exception {
        when(announcementService.list(999L, 42L)).thenThrow(new com.ubb.events.common.errors.NotFoundException("Event not found"));
        mockMvc.perform(get("/api/events/999/announcements").header("Authorization", "Bearer " + tokenService.issue(42L)))
                .andExpect(status().isNotFound());
    }

    @Test void memberCanPublishAnnouncement() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(announcementService.create(eq(7L), eq(3L), any())).thenReturn(new AnnouncementResponse(9L, 3L, "Title", "Content"));
        mockMvc.perform(post("/api/organizations/7/events/3/announcements").header("Authorization", "Bearer " + tokenService.issue(42L)).contentType(APPLICATION_JSON).content("{\"title\":\"Title\",\"content\":\"Content\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(9)).andExpect(jsonPath("$.eventId").value(3))
                .andExpect(jsonPath("$.title").value("Title")).andExpect(jsonPath("$.content").value("Content"));
    }

    @Test void managerCanPublishAnnouncement() throws Exception {
        when(authorizationService.hasAtLeast(43L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        when(announcementService.create(eq(7L), eq(3L), any())).thenReturn(new AnnouncementResponse(10L, 3L, "Manager title", "Manager content"));
        mockMvc.perform(post("/api/organizations/7/events/3/announcements").header("Authorization", "Bearer " + tokenService.issue(43L)).contentType(APPLICATION_JSON).content("{\"title\":\"Manager title\",\"content\":\"Manager content\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Manager title"));
    }

    @Test void blankTitleIsRejected() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        mockMvc.perform(post("/api/organizations/7/events/3/announcements").header("Authorization", "Bearer " + tokenService.issue(42L)).contentType(APPLICATION_JSON).content("{\"title\":\" \",\"content\":\"Content\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(announcementService);
    }

    @Test void blankContentIsRejected() throws Exception {
        when(authorizationService.hasAtLeast(42L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(true);
        mockMvc.perform(post("/api/organizations/7/events/3/announcements").header("Authorization", "Bearer " + tokenService.issue(42L)).contentType(APPLICATION_JSON).content("{\"title\":\"Title\",\"content\":\" \"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(announcementService);
    }

    @Test void unauthenticatedIsRejected() throws Exception {
        mockMvc.perform(post("/api/organizations/7/events/3/announcements").contentType(APPLICATION_JSON).content("{\"title\":\"Title\",\"content\":\"Content\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(announcementService);
    }

    @Test void nonMemberIsForbidden() throws Exception {
        when(authorizationService.hasAtLeast(44L, 7L, EffectiveOrgRole.MEMBER)).thenReturn(false);
        mockMvc.perform(post("/api/organizations/7/events/3/announcements").header("Authorization", "Bearer " + tokenService.issue(44L)).contentType(APPLICATION_JSON).content("{\"title\":\"Title\",\"content\":\"Content\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(announcementService);
    }
}
