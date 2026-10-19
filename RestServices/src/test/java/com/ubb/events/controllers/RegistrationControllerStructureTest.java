package com.ubb.events.controllers;

import com.ubb.events.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(RegistrationController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/registrations", RegistrationController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, RegistrationController.class.getDeclaredMethods().length);
        Constructor<?> constructor = RegistrationController.class.getDeclaredConstructor(RegistrationService.class);
        assertNotNull(constructor);
    }
}
