package com.ubb.events.controllers;

import com.ubb.events.EventService;
import com.ubb.events.security.NoOrgCheck;
import com.ubb.events.security.RequireOrgPermission;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class DiscoverEventControllerStructureTest {
    @Test
    void controllerIsMappedOutsideOrganizationPathWithServiceDependency() throws Exception {
        assertTrue(DiscoverEventController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/events", DiscoverEventController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertNotNull(DiscoverEventController.class.getDeclaredConstructor(EventService.class));
    }

    @Test
    void discoverHandlerIsGetWithoutOrganizationAnnotations() throws Exception {
        Method method = DiscoverEventController.class.getDeclaredMethod("discoverEvents");
        assertNotNull(method.getAnnotation(GetMapping.class), "discoverEvents must be a GET handler");
        assertFalse(method.isAnnotationPresent(RequireOrgPermission.class));
        assertFalse(method.isAnnotationPresent(NoOrgCheck.class));
    }
}
