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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void authenticatedUserCanUpdateNamesAndEmailRemainsImmutable() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"Before\",\"lastName\":\"Name\",\"email\":\"t11-update@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"email\":\"t11-update@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String jwt = new com.fasterxml.jackson.databind.ObjectMapper().readTree(login).get("token").asText();

        mockMvc.perform(put("/api/users/me?userId=999999").header("Authorization", "Bearer " + jwt)
                        .contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"After\",\"lastName\":\"Updated\",\"email\":\"changed@example.com\",\"userId\":999999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("After"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.email").value("t11-update@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/api/users/me?userId=999999")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("After"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.email").value("t11-update@example.com"));
    }

    @Test
    void updateRejectsMissingNameFieldsWithSharedValidationErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"Validation\",\"lastName\":\"User\",\"email\":\"t11-validation@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"email\":\"t11-validation@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String jwt = new com.fasterxml.jackson.databind.ObjectMapper().readTree(login).get("token").asText();

        mockMvc.perform(put("/api/users/me").header("Authorization", "Bearer " + jwt)
                        .contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'firstName')]").isNotEmpty())
                .andExpect(jsonPath("$.errors[?(@.field == 'lastName')]").isNotEmpty());
    }

    @Test
    void updateRequiresAuthentication() throws Exception {
        mockMvc.perform(put("/api/users/me").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"After\",\"lastName\":\"Updated\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedTokenOnUpdateReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/users/me").header("Authorization", "Bearer tampered")
                        .contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"After\",\"lastName\":\"Updated\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void validTokenForMissingUserOnUpdateReturnsSharedNotFoundResponse() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + tokenService.issue(999_999L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"After\",\"lastName\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
