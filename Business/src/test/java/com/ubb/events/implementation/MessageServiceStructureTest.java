package com.ubb.events.implementation;

import com.ubb.events.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndHasNoOperations() {
        assertTrue(MessageServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(MessageService.class.isAssignableFrom(MessageServiceImpl.class));
        assertEquals(0, MessageService.class.getDeclaredMethods().length);
        assertEquals(0, MessageServiceImpl.class.getDeclaredMethods().length);
    }
}
