package com.ubb.events.security;

import com.ubb.events.EffectiveOrgRole;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorizationAnnotationTest {
    @Test
    void requiredPermissionDefaultsToMember() throws Exception {
        Method method = SampleController.class.getDeclaredMethod("endpoint");
        assertEquals(EffectiveOrgRole.MEMBER,
                method.getAnnotation(RequireOrgPermission.class).value());
    }

    private static class SampleController {
        @RequireOrgPermission
        void endpoint() { }
    }
}
