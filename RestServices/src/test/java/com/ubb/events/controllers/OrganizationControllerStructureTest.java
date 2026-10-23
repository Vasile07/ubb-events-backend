package com.ubb.events.controllers;

import com.ubb.events.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.ubb.events.security.NoOrgCheck;

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

    @Test
    void detailsHandlerIsAuthenticatedAndSkipsOrganizationAuthorization() throws Exception {
        var method = OrganizationController.class.getDeclaredMethod("details", Long.class, java.security.Principal.class);
        assertEquals("/{organizationId}", method.getAnnotation(GetMapping.class).value()[0]);
        assertTrue(method.isAnnotationPresent(NoOrgCheck.class));
    }
}
