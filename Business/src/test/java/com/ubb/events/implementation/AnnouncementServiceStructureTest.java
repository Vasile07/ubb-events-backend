package com.ubb.events.implementation;

import com.ubb.events.AnnouncementService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndHasNoOperations() {
        assertTrue(AnnouncementServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(AnnouncementService.class.isAssignableFrom(AnnouncementServiceImpl.class));
        assertEquals(0, AnnouncementService.class.getDeclaredMethods().length);
        assertEquals(0, AnnouncementServiceImpl.class.getDeclaredMethods().length);
    }
}
