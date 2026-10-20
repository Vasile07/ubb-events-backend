package com.ubb.events.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubb.events.EffectiveOrgRole;
import com.ubb.events.OrganizationAuthorizationService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizationAuthorizationInterceptorTest {
    private final OrganizationAuthorizationService service = mock(OrganizationAuthorizationService.class);
    private final OrganizationAuthorizationInterceptor interceptor =
            new OrganizationAuthorizationInterceptor(service, new ObjectMapper());

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void readsNumericOrganizationIdFromPathAndPrincipal() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null));
        MockHttpServletRequest request = requestFor(12L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(service.hasAtLeast(7L, 12L, EffectiveOrgRole.MEMBER)).thenReturn(true);

        assertTrue(interceptor.preHandle(request, response, handler("member")));
        verify(service).hasAtLeast(7L, 12L, EffectiveOrgRole.MEMBER);
    }

    @Test
    void noOrgCheckSkipsMembershipLookup() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(requestFor(12L), response, handler("publicEndpoint")));
        verifyNoInteractions(service);
    }

    @Test
    void missingAnnotationReturnsForbidden() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(requestFor(12L), response, handler("unannotated")));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void insufficientRoleReturnsSharedForbiddenResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null));
        when(service.hasAtLeast(7L, 12L, EffectiveOrgRole.MANAGER)).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(requestFor(12L), response, handler("manager")));
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals(403, new ObjectMapper().readTree(response.getContentAsString()).get("status").asInt());
        assertEquals("Organization access is forbidden",
                new ObjectMapper().readTree(response.getContentAsString()).get("message").asText());
    }

    @Test
    void organizationScopedProductionHandlersDeclareAuthorization() {
        for (Class<?> controller : new Class<?>[]{
                com.ubb.events.controllers.OrganizationController.class,
                com.ubb.events.controllers.MembershipController.class,
                com.ubb.events.controllers.MessageController.class,
                com.ubb.events.controllers.RegistrationController.class}) {
            for (var method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(org.springframework.web.bind.annotation.RequestMapping.class)
                        || method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)
                        || method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)
                        || method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)
                        || method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class)
                        || method.isAnnotationPresent(org.springframework.web.bind.annotation.PatchMapping.class)) {
                    assertTrue(method.isAnnotationPresent(RequireOrgPermission.class)
                                    || method.isAnnotationPresent(NoOrgCheck.class),
                            () -> controller.getSimpleName() + "." + method.getName() + " lacks org authorization");
                }
            }
        }
    }

    private MockHttpServletRequest requestFor(long organizationId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                Map.of("organizationId", Long.toString(organizationId)));
        return request;
    }

    private HandlerMethod handler(String name) throws NoSuchMethodException {
        return new HandlerMethod(new TestController(), TestController.class.getDeclaredMethod(name));
    }

    @RestController
    @RequestMapping("/api/organizations/{organizationId}/test")
    private static class TestController {
        @GetMapping("/member")
        @RequireOrgPermission void member() { }
        @GetMapping("/manager")
        @RequireOrgPermission(EffectiveOrgRole.MANAGER) void manager() { }
        @GetMapping("/public")
        @NoOrgCheck void publicEndpoint() { }
        @GetMapping("/unannotated")
        void unannotated() { }
    }
}
