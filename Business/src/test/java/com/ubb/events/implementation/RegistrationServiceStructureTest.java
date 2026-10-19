package com.ubb.events.implementation;

import com.ubb.events.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndHasNoOperations() {
        assertTrue(RegistrationServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(RegistrationService.class.isAssignableFrom(RegistrationServiceImpl.class));
        assertEquals(0, RegistrationService.class.getDeclaredMethods().length);
        assertEquals(0, RegistrationServiceImpl.class.getDeclaredMethods().length);
    }
}
