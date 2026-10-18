package com.ubb.events;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.ubb.events.errors.MockErrorController;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockErrorController.class)
class BackendApiDocumentationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentDescribesHealthEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/api/health")));
    }

    @Test
    void openApiDocumentDescribesSharedErrorSchema() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ErrorResponse")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/test/errors/business")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"404\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"409\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"403\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"500\"")));
    }
}
