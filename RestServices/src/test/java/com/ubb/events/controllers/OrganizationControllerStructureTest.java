package com.ubb.events.controllers;

import com.ubb.events.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithListHandler() throws Exception {
        assertTrue(OrganizationController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/organizations",
                OrganizationController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertNotNull(OrganizationController.class.getDeclaredMethod("list", java.security.Principal.class));
        Constructor<?> constructor = OrganizationController.class.getDeclaredConstructor(OrganizationService.class);
        assertNotNull(constructor);
    }
}
