package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.EventTaskService;
import com.ubb.events.security.RequireOrgPermission;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class EventTaskControllerStructureTest {
    @Test
    void controllerIsOrganizationScopedWithServiceDependency() throws Exception {
        assertTrue(EventTaskController.class.isAnnotationPresent(RestController.class));
        assertNotNull(EventTaskController.class.getAnnotation(RequestMapping.class));
        assertEquals("/api/organizations", EventTaskController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertNotNull(EventTaskController.class.getDeclaredConstructor(EventTaskService.class));
    }

    @Test
    void listHandlerIsMappedForOrganizationMembers() throws Exception {
        Method method = EventTaskController.class.getDeclaredMethod("listEventTasks", Long.class, Long.class);
        assertNotNull(method.getAnnotation(GetMapping.class), "listEventTasks must be a GET handler");
        assertEquals("/{organizationId}/events/{eventId}/tasks", method.getAnnotation(GetMapping.class).value()[0]);
        assertNotNull(method.getAnnotation(RequireOrgPermission.class), "listEventTasks must require organization permission");
        assertEquals(EffectiveOrgRole.MEMBER, method.getAnnotation(RequireOrgPermission.class).value());
    }
}
