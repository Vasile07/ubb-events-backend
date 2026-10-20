package com.ubb.events.implementation;

import com.ubb.events.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndHasNoOperations() {
        assertTrue(EventServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(EventService.class.isAssignableFrom(EventServiceImpl.class));
        assertEquals(0, EventService.class.getDeclaredMethods().length);
        assertEquals(0, EventServiceImpl.class.getDeclaredMethods().length);
    }
}
