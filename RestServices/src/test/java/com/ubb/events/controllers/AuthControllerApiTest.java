package com.ubb.events.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerApiTest {
    @Autowired MockMvc mockMvc;

    @Test
    void registersUserAndDoesNotReturnPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"ada-api@example.com\",\"password\":\"Str0ng!Pass\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Ada"))
                .andExpect(jsonPath("$.lastName").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada-api@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectsWeakPasswordWithFieldError() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"weak-api@example.com\",\"password\":\"weakpass\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("password")));
    }

    @ParameterizedTest(name = "rejects password policy violation: {0}")
    @CsvSource({
            "minimum length, A1bc!de",
            "number, Abcdefg!",
            "uppercase, abc1def!",
            "lowercase, ABC1DEF!",
            "special character, Abc1defg"
    })
    void rejectsEachPasswordPolicyViolation(String rule, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"" + rule.replace(" ", "-") + "@example.com\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("password")));
    }

    @Test
    void rejectsDuplicateEmailWithConflict() throws Exception {
        String payload = "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"duplicate-api@example.com\",\"password\":\"Str0ng!Pass\"}";
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
