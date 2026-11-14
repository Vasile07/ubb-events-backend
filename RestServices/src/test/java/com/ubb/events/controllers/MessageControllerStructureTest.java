package com.ubb.events.controllers;

import com.ubb.events.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MessageControllerStructureTest {
    @Test
    void controllerDefinesOrganizationRootMappingAndServiceDependency() throws Exception {
        assertTrue(MessageController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/organizations", MessageController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertTrue(MessageController.class.getDeclaredMethods().length >= 2);
        Constructor<?> constructor = MessageController.class.getDeclaredConstructor(MessageService.class);
        assertNotNull(constructor);
    }
}
