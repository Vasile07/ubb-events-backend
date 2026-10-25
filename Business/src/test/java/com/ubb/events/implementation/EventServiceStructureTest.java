package com.ubb.events.implementation;

import com.ubb.events.EventService;
import com.ubb.events.persistence.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndDependsOnRepository() throws Exception {
        assertTrue(EventServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(EventService.class.isAssignableFrom(EventServiceImpl.class));
        assertNotNull(EventServiceImpl.class.getDeclaredConstructor(EventRepository.class));
    }

    @Test
    void serviceDeclaresOrganizationEventReadOperations() throws Exception {
        assertNotNull(EventService.class.getDeclaredMethod("listOrganizationEvents", Long.class));
        assertNotNull(EventService.class.getDeclaredMethod("getOrganizationEvent", Long.class, Long.class));
    }
}
