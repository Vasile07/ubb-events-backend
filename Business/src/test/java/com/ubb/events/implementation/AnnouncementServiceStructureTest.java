package com.ubb.events.implementation;

import com.ubb.events.AnnouncementService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndExposesCreateOperation() {
        assertTrue(AnnouncementServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(AnnouncementService.class.isAssignableFrom(AnnouncementServiceImpl.class));
        assertTrue(java.util.Arrays.stream(AnnouncementService.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("create")));
        assertTrue(java.util.Arrays.stream(AnnouncementServiceImpl.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("create")));
    }
}
