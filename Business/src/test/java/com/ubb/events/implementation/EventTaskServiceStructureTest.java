package com.ubb.events.implementation;

import com.ubb.events.EventTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class EventTaskServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndHasNoOperations() {
        assertTrue(EventTaskServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(EventTaskService.class.isAssignableFrom(EventTaskServiceImpl.class));
        assertEquals(0, EventTaskService.class.getDeclaredMethods().length);
        assertEquals(0, EventTaskServiceImpl.class.getDeclaredMethods().length);
    }
}
