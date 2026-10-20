package com.ubb.events.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.ubb.events.common.security.JwtTokenService;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService tokenService;

    @Test
    void profileRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileReturnsPublicFieldsAndMembershipArray() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"T10\",\"lastName\":\"User\",\"email\":\"t10-profile@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isCreated());
        String token = mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"email\":\"t10-profile@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String jwt = new com.fasterxml.jackson.databind.ObjectMapper().readTree(token).get("token").asText();
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("T10"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("t10-profile@example.com"))
                .andExpect(jsonPath("$.memberships").isArray())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void malformedTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer tampered"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void validTokenForMissingUserReturnsSharedNotFoundResponse() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + tokenService.issue(999_999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void requestInputCannotSelectAnotherProfile() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"Input\",\"lastName\":\"Safe\",\"email\":\"t10-input-safe@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"email\":\"t10-input-safe@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String jwt = new com.fasterxml.jackson.databind.ObjectMapper().readTree(login).get("token").asText();

        mockMvc.perform(get("/api/users/me?userId=999999")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(APPLICATION_JSON)
                        .content("{\"userId\":999999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("t10-input-safe@example.com"));
    }
}
