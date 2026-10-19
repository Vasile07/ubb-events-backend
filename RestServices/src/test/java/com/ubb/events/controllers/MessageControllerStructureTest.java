package com.ubb.events.controllers;

import com.ubb.events.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MessageControllerStructureTest {
    @Test
    void controllerDefinesRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(MessageController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/chat-messages", MessageController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, MessageController.class.getDeclaredMethods().length);
        Constructor<?> constructor = MessageController.class.getDeclaredConstructor(MessageService.class);
        assertNotNull(constructor);
    }
}
