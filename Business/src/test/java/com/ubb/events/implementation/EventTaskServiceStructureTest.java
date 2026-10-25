package com.ubb.events.implementation;

import com.ubb.events.EventTaskService;
import com.ubb.events.persistence.EventRepository;
import com.ubb.events.persistence.EventTaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class EventTaskServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndDependsOnRepositories() throws Exception {
        assertTrue(EventTaskServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(EventTaskService.class.isAssignableFrom(EventTaskServiceImpl.class));
        assertNotNull(EventTaskServiceImpl.class.getDeclaredConstructor(EventTaskRepository.class, EventRepository.class));
    }

    @Test
    void serviceDeclaresEventTaskReadOperation() throws Exception {
        assertNotNull(EventTaskService.class.getDeclaredMethod("listEventTasks", Long.class, Long.class));
    }
}
