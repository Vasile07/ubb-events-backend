package com.ubb.events.controllers;

import com.ubb.events.MembershipService;
import com.ubb.events.EffectiveOrgRole;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import static org.junit.jupiter.api.Assertions.*;

class MembershipControllerStructureTest {
    @Test
    void controllerDefinesOrganizationMembershipRequestHandler() throws Exception {
        assertTrue(MembershipController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/organizations", MembershipController.class.getAnnotation(RequestMapping.class).value()[0]);
        Constructor<?> constructor = MembershipController.class.getDeclaredConstructor(MembershipService.class);
        assertNotNull(constructor);
        Method method = MembershipController.class.getDeclaredMethod("listMembershipRequests", Long.class, com.ubb.events.domain.MembershipStatus.class);
        assertEquals("/{organizationId}/membership-requests", method.getAnnotation(GetMapping.class).value()[0]);
        assertEquals(EffectiveOrgRole.MANAGER, method.getAnnotation(com.ubb.events.security.RequireOrgPermission.class).value());
    }

    @Test
    void patchDecisionHandlerRequiresManagerPermission() throws Exception {
        Method method = MembershipController.class.getDeclaredMethod("decideMembershipRequest", Long.class, Long.class,
                com.ubb.events.dto.MembershipDecisionRequest.class);
        assertEquals("/{organizationId}/membership-requests/{requestId}", method.getAnnotation(PatchMapping.class).value()[0]);
        assertEquals(EffectiveOrgRole.MANAGER, method.getAnnotation(com.ubb.events.security.RequireOrgPermission.class).value());
    }

    @Test
    void membersHandlerUsesMemberPermission() throws Exception {
        Method method = MembershipController.class.getDeclaredMethod("listOrganizationMembers", Long.class);
        assertEquals("/{organizationId}/members", method.getAnnotation(GetMapping.class).value()[0]);
        assertEquals(EffectiveOrgRole.MEMBER,
                method.getAnnotation(com.ubb.events.security.RequireOrgPermission.class).value());
    }
}
