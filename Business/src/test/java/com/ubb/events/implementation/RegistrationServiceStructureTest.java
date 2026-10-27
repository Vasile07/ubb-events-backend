package com.ubb.events.implementation;

import com.ubb.events.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndExposesRegistrationOperation() throws NoSuchMethodException {
        assertTrue(RegistrationServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(RegistrationService.class.isAssignableFrom(RegistrationServiceImpl.class));
        assertNotNull(RegistrationService.class.getDeclaredMethod("registerForEvent", Long.class, Long.class));
        assertNotNull(RegistrationServiceImpl.class.getDeclaredConstructor(com.ubb.events.persistence.EventRepository.class, com.ubb.events.persistence.RegistrationRepository.class, com.ubb.events.persistence.UserRepository.class));
    }
}
