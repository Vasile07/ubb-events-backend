package com.ubb.events.controllers;

import com.ubb.events.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class EventControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(EventController.class.isAnnotationPresent(RestController.class));
        assertNotNull(EventController.class.getAnnotation(RequestMapping.class));
        assertEquals("/api/events", EventController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, EventController.class.getDeclaredMethods().length);
        Constructor<?> constructor = EventController.class.getDeclaredConstructor(EventService.class);
        assertNotNull(constructor);
    }
}
