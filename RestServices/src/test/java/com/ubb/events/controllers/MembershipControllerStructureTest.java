package com.ubb.events.controllers;

import com.ubb.events.MembershipService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MembershipControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(MembershipController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/memberships", MembershipController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, MembershipController.class.getDeclaredMethods().length);
        Constructor<?> constructor = MembershipController.class.getDeclaredConstructor(MembershipService.class);
        assertNotNull(constructor);
    }
}
