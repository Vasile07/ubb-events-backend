package com.ubb.events.errors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockErrorController.class)
class GlobalErrorHandlingApiTest {
    @Autowired MockMvc mockMvc;

    @Test void mapsBusinessRuleToBadRequest() throws Exception { assertError("/business", 400, "Rule failed"); }
    @Test void mapsNotFound() throws Exception { assertError("/not-found", 404, "Missing resource"); }
    @Test void mapsConflict() throws Exception { assertError("/conflict", 409, "State conflict"); }
    @Test void mapsForbidden() throws Exception { assertError("/forbidden", 403, "Access denied"); }
    @Test void mapsValidationException() throws Exception { assertError("/validation-exception", 400, "Invalid operation"); }

    @Test void mapsUnexpectedToSafeInternalServerError() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(content().string(not(containsString("secret implementation detail"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    @Test void reportsBeanValidationFieldErrorsWithoutStackTrace() throws Exception {
        mockMvc.perform(post("/test/errors/validate").contentType(APPLICATION_JSON).content("{\"value\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("value"))
                .andExpect(jsonPath("$.errors[0].message").value("value is required"))
                .andExpect(content().string(not(containsString("stackTrace"))));
    }

    @Test void validRequestReachesMockController() throws Exception {
        mockMvc.perform(post("/test/errors/validate").contentType(APPLICATION_JSON).content("{\"value\":\"ok\"}"))
                .andExpect(status().isOk()).andExpect(content().string("ok"));
    }

    private void assertError(String path, int status, String message) throws Exception {
        mockMvc.perform(get("/test/errors" + path))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(0)));
    }
}
