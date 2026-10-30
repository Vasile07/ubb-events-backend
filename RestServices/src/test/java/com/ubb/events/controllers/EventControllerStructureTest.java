package com.ubb.events.controllers;

import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.EventService;
import com.ubb.events.security.RequireOrgPermission;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class EventControllerStructureTest {
    @Test
    void controllerIsOrganizationScopedWithServiceDependency() throws Exception {
        assertTrue(EventController.class.isAnnotationPresent(RestController.class));
        assertNotNull(EventController.class.getAnnotation(RequestMapping.class));
        assertEquals("/api/organizations", EventController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertNotNull(EventController.class.getDeclaredConstructor(EventService.class));
    }

    @Test
    void listHandlerIsMappedForOrganizationMembers() throws Exception {
        assertMemberGet(EventController.class.getDeclaredMethod("listOrganizationEvents", Long.class),
                "/{organizationId}/events");
    }

    @Test
    void detailHandlerIsMappedForOrganizationMembers() throws Exception {
        assertMemberGet(EventController.class.getDeclaredMethod("getOrganizationEvent", Long.class, Long.class),
                "/{organizationId}/events/{eventId}");
    }

    @Test
    void publishHandlerIsMappedForManagersOnly() throws Exception {
        Method method = EventController.class.getDeclaredMethod("publishEvent", Long.class, Long.class);
        assertNotNull(method.getAnnotation(PatchMapping.class), "publishEvent must be a PATCH handler");
        assertEquals("/{organizationId}/events/{eventId}/publish", method.getAnnotation(PatchMapping.class).value()[0]);
        assertNotNull(method.getAnnotation(RequireOrgPermission.class), "publishEvent must require organization permission");
        assertEquals(EffectiveOrgRole.MANAGER, method.getAnnotation(RequireOrgPermission.class).value());
    }

    @Test
    void createHandlerIsMappedForManagersOnly() throws Exception {
        Method method = EventController.class.getDeclaredMethod("createEvent", Long.class,
                com.ubb.events.dto.CreateEventRequest.class);
        assertNotNull(method.getAnnotation(PostMapping.class));
        assertEquals("/{organizationId}/events", method.getAnnotation(PostMapping.class).value()[0]);
        assertNotNull(method.getAnnotation(RequireOrgPermission.class));
        assertEquals(EffectiveOrgRole.MANAGER, method.getAnnotation(RequireOrgPermission.class).value());
        assertNotNull(method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class));
        assertNotNull(method.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponses.class));
    }

    @Test
    void registrationsHandlerIsMappedForOrganizationMembers() throws Exception {
        Method method = EventController.class.getDeclaredMethod("listEventRegistrations", Long.class, Long.class);
        assertNotNull(method.getAnnotation(GetMapping.class));
        assertEquals("/{organizationId}/events/{eventId}/registrations", method.getAnnotation(GetMapping.class).value()[0]);
        assertNotNull(method.getAnnotation(RequireOrgPermission.class));
        assertEquals(EffectiveOrgRole.MEMBER, method.getAnnotation(RequireOrgPermission.class).value());
        assertNotNull(method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class));
        assertNotNull(method.getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponses.class));
    }

    private static void assertMemberGet(Method method, String path) {
        assertNotNull(method.getAnnotation(GetMapping.class), method.getName() + " must be a GET handler");
        assertEquals(path, method.getAnnotation(GetMapping.class).value()[0]);
        assertNotNull(method.getAnnotation(RequireOrgPermission.class), method.getName() + " must require organization permission");
        assertEquals(EffectiveOrgRole.MEMBER, method.getAnnotation(RequireOrgPermission.class).value());
    }
}
