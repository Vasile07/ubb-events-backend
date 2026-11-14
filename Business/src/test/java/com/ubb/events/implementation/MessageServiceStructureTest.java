package com.ubb.events.implementation;

import com.ubb.events.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndExposesMessageListing() {
        assertTrue(MessageServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(MessageService.class.isAssignableFrom(MessageServiceImpl.class));
        Method method = assertDoesNotThrow(() -> MessageService.class.getMethod("listEventMessages", Long.class, Long.class));
        assertEquals(java.util.List.class, method.getReturnType());
        assertDoesNotThrow(() -> MessageServiceImpl.class.getMethod("listEventMessages", Long.class, Long.class));
    }
}
