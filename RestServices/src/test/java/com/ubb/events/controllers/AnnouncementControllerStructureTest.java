package com.ubb.events.controllers;

import com.ubb.events.AnnouncementService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(AnnouncementController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/announcements", AnnouncementController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, AnnouncementController.class.getDeclaredMethods().length);
        Constructor<?> constructor = AnnouncementController.class.getDeclaredConstructor(AnnouncementService.class);
        assertNotNull(constructor);
        assertNotNull(AnnouncementController.class.getDeclaredField("announcementService"));
    }
}
