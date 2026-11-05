package com.ubb.events.controllers;

import com.ubb.events.FollowUpService;
import com.ubb.events.EffectiveOrgRole;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import com.ubb.events.security.RequireOrgPermission;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpControllerStructureTest {
    @Test
    void controllerDefinesManagerOnlyCreationHandler() throws Exception {
        assertTrue(FollowUpController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/organizations", FollowUpController.class.getAnnotation(RequestMapping.class).value()[0]);
        Constructor<?> constructor = FollowUpController.class.getDeclaredConstructor(FollowUpService.class);
        assertNotNull(constructor);
        var method = FollowUpController.class.getDeclaredMethod("create", Long.class, Long.class,
                com.ubb.events.dto.CreateFollowUpRequest.class);
        assertEquals("/{organizationId}/events/{eventId}/follow-up", method.getAnnotation(PostMapping.class).value()[0]);
        assertEquals(EffectiveOrgRole.MANAGER, method.getAnnotation(RequireOrgPermission.class).value());
    }
}
