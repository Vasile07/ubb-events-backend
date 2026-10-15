package com.ubb.events;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvConfigurationTest {
    @Test
    void optionalEnvFileIsImportedFromBackendRoot() throws IOException {
        String properties = Files.readString(Path.of("src", "main", "resources", "application.properties"));
        String build = Files.readString(Path.of("build.gradle"));

        assertTrue(properties.contains("spring.config.import=optional:file:.env[.properties]"));
        assertTrue(build.contains("workingDir rootProject.projectDir"));
    }
}
