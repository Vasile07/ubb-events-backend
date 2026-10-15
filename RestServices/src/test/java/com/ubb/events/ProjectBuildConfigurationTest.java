package com.ubb.events;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectBuildConfigurationTest {
    private static final List<String> PROJECTS = List.of("Business", "RestServices", "Persistence", "Model", "Common");

    @Test
    void eachGradleProjectOwnsItsBuildConfiguration() {
        for (String project : PROJECTS) {
            Path buildFile = Path.of("..", project, "build.gradle");
            assertTrue(Files.exists(buildFile), project + " must own a build.gradle");
            assertTrue(read(buildFile).contains("java-library"), project + " must configure the Java library plugin");
        }
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }
}
