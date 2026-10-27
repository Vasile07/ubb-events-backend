package com.ubb.events.controllers;

import com.ubb.events.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationControllerStructureTest {
    @Test
    void controllerMapsRegistrationEndpointAndInjectsService() throws Exception {
        assertTrue(RegistrationController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/events", RegistrationController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/{eventId}/registrations", RegistrationController.class.getDeclaredMethod("register", Long.class, org.springframework.security.core.Authentication.class).getAnnotation(PostMapping.class).value()[0]);
        Constructor<?> constructor = RegistrationController.class.getDeclaredConstructor(RegistrationService.class);
        assertNotNull(constructor);
    }
}
