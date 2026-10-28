package com.ubb.events.controllers;

import com.ubb.events.RegistrationService;
import com.ubb.events.dto.RegistrationDetailsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserRegistrationControllerTest {
    @Test
    void delegatesToAuthenticatedUserAndReturnsRegistrations() {
        RegistrationService service = mock(RegistrationService.class);
        UserRegistrationController controller = new UserRegistrationController(service);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(42L);
        when(service.listMyRegistrations(42L)).thenReturn(List.of(mock(RegistrationDetailsResponse.class)));

        var response = controller.registrations(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(service).listMyRegistrations(42L);
    }
}
