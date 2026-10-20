package com.ubb.events.controllers;

import com.ubb.events.FollowUpService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(FollowUpController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/followups", FollowUpController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, FollowUpController.class.getDeclaredMethods().length);
        Constructor<?> constructor = FollowUpController.class.getDeclaredConstructor(FollowUpService.class);
        assertNotNull(constructor);
    }
}
