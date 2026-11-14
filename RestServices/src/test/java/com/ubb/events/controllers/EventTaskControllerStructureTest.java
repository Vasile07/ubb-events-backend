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

    @Test
    void taskWriteHandlersAreMappedForManagersOnly() throws Exception {
        Class<?> request = com.ubb.events.dto.EventTaskRequest.class;
        Method add = EventTaskController.class.getDeclaredMethod("addTask", Long.class, Long.class, request);
        assertNotNull(add.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class), "addTask must be a POST handler");
        assertEquals("/{organizationId}/events/{eventId}/tasks",
                add.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class).value()[0]);
        assertManager(add);

        Method update = EventTaskController.class.getDeclaredMethod("updateTask", Long.class, Long.class, Long.class, request);
        assertNotNull(update.getAnnotation(org.springframework.web.bind.annotation.PutMapping.class), "updateTask must be a PUT handler");
        assertEquals("/{organizationId}/events/{eventId}/tasks/{taskId}",
                update.getAnnotation(org.springframework.web.bind.annotation.PutMapping.class).value()[0]);
        assertManager(update);

        Method remove = EventTaskController.class.getDeclaredMethod("removeTask", Long.class, Long.class, Long.class);
        assertNotNull(remove.getAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class), "removeTask must be a DELETE handler");
        assertEquals("/{organizationId}/events/{eventId}/tasks/{taskId}",
                remove.getAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class).value()[0]);
        assertManager(remove);
    }

    private static void assertManager(Method method) {
        assertNotNull(method.getAnnotation(RequireOrgPermission.class), method.getName() + " must require organization permission");
        assertEquals(EffectiveOrgRole.MANAGER, method.getAnnotation(RequireOrgPermission.class).value());
        assertNotNull(method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class));
        assertNotNull(method.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponses.class));
    }
}
