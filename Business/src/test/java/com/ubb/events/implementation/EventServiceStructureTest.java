package com.ubb.events.implementation;

import com.ubb.events.EventService;
import com.ubb.events.persistence.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndDependsOnRepositoryAndClock() throws Exception {
        assertTrue(EventServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(EventService.class.isAssignableFrom(EventServiceImpl.class));
        assertNotNull(EventServiceImpl.class.getDeclaredConstructor(EventRepository.class, Clock.class));
    }

    @Test
    void serviceDeclaresOrganizationEventReadOperations() throws Exception {
        assertNotNull(EventService.class.getDeclaredMethod("listOrganizationEvents", Long.class));
        assertNotNull(EventService.class.getDeclaredMethod("getOrganizationEvent", Long.class, Long.class));
    }

    @Test
    void serviceDeclaresDiscoverOperation() throws Exception {
        assertNotNull(EventService.class.getDeclaredMethod("discoverEvents", com.ubb.events.dto.EventDiscoveryFilter.class));
    }

    @Test
    void publishIsTransactional() throws Exception {
        assertNotNull(EventService.class.getDeclaredMethod("publishEvent", Long.class, Long.class));
        assertTrue(EventServiceImpl.class.getDeclaredMethod("publishEvent", Long.class, Long.class)
                .isAnnotationPresent(Transactional.class));
    }
}
