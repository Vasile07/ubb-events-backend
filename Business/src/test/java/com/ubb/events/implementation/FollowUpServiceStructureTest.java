package com.ubb.events.implementation;

import com.ubb.events.FollowUpService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndHasNoOperations() {
        assertTrue(FollowUpServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(FollowUpService.class.isAssignableFrom(FollowUpServiceImpl.class));
        assertEquals(0, FollowUpService.class.getDeclaredMethods().length);
        assertEquals(0, FollowUpServiceImpl.class.getDeclaredMethods().length);
    }
}
