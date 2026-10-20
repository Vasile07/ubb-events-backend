package com.ubb.events.controllers;

import com.ubb.events.EventTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class EventTaskControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(EventTaskController.class.isAnnotationPresent(RestController.class));
        assertNotNull(EventTaskController.class.getAnnotation(RequestMapping.class));
        assertEquals("/api/event-tasks", EventTaskController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, EventTaskController.class.getDeclaredMethods().length);
        Constructor<?> constructor = EventTaskController.class.getDeclaredConstructor(EventTaskService.class);
        assertNotNull(constructor);
    }
}
