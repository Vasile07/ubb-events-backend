package com.ubb.events.controllers;

import com.ubb.events.EventService;
import com.ubb.events.security.NoOrgCheck;
import com.ubb.events.security.RequireOrgPermission;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscoverEventControllerStructureTest {
    @Test
    void controllerIsMappedOutsideOrganizationPathWithServiceDependency() throws Exception {
        assertTrue(DiscoverEventController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/events", DiscoverEventController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertNotNull(DiscoverEventController.class.getDeclaredConstructor(EventService.class));
    }

    @Test
    void discoverHandlerIsGetWithOptionalFilterParametersAndNoOrganizationAnnotations() throws Exception {
        Method method = DiscoverEventController.class.getDeclaredMethod(
                "discoverEvents", String.class, Long.class, LocalDate.class, LocalDate.class);
        assertNotNull(method.getAnnotation(GetMapping.class), "discoverEvents must be a GET handler");
        assertFalse(method.isAnnotationPresent(RequireOrgPermission.class));
        assertFalse(method.isAnnotationPresent(NoOrgCheck.class));

        List<String> names = Arrays.stream(method.getParameters())
                .map(parameter -> {
                    RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                    assertNotNull(requestParam, parameter.getName() + " must be a query parameter");
                    assertFalse(requestParam.required(), parameter.getName() + " must be optional");
                    return requestParam.value().isEmpty() ? requestParam.name() : requestParam.value();
                })
                .toList();
        assertEquals(List.of("search", "organizationId", "from", "to"), names);
    }
}
