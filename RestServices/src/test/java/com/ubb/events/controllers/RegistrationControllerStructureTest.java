package com.ubb.events.controllers;

import com.ubb.events.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationControllerStructureTest {
    @Test
    void controllerMapsRegistrationEndpointAndInjectsService() throws Exception {
        assertTrue(RegistrationController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/events", RegistrationController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/{eventId}/registrations", RegistrationController.class.getDeclaredMethod("register", Long.class, org.springframework.security.core.Authentication.class).getAnnotation(PostMapping.class).value()[0]);
        Constructor<?> constructor = RegistrationController.class.getDeclaredConstructor(RegistrationService.class);
        assertNotNull(constructor);
    }

    @Test
    void controllerMapsCancellationEndpoint() throws Exception {
        var method = RegistrationController.class.getDeclaredMethod("cancel", Long.class, org.springframework.security.core.Authentication.class);
        assertEquals("/{eventId}/registrations/me", method.getAnnotation(DeleteMapping.class).value()[0]);
        assertNotNull(method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class));
    }

    @Test
    void cancellationReturnsNoContentAndUsesAuthenticatedUser() {
        RegistrationService service = mock(RegistrationService.class);
        RegistrationController controller = new RegistrationController(service);
        var authentication = mock(org.springframework.security.core.Authentication.class);
        when(authentication.getPrincipal()).thenReturn(42L);

        var response = controller.cancel(7L, authentication);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(service).cancelRegistration(7L, 42L);
    }
}
