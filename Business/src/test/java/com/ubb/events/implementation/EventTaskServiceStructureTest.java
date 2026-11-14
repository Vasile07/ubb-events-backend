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
        assertNotNull(EventTaskServiceImpl.class.getDeclaredConstructor(EventTaskRepository.class, EventRepository.class,
                com.ubb.events.persistence.MembershipRepository.class, java.time.Clock.class));
    }

    @Test
    void taskWriteOperationsAreTransactional() throws Exception {
        Class<?> request = com.ubb.events.dto.EventTaskRequest.class;
        assertTrue(EventTaskServiceImpl.class.getDeclaredMethod("addTask", Long.class, Long.class, request)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
        assertTrue(EventTaskServiceImpl.class.getDeclaredMethod("updateTask", Long.class, Long.class, Long.class, request)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
        assertTrue(EventTaskServiceImpl.class.getDeclaredMethod("removeTask", Long.class, Long.class, Long.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
    }

    @Test
    void serviceDeclaresEventTaskReadOperation() throws Exception {
        assertNotNull(EventTaskService.class.getDeclaredMethod("listEventTasks", Long.class, Long.class));
    }

    @Test
    void serviceDeclaresMyTasksReadOperation() throws Exception {
        assertNotNull(EventTaskService.class.getDeclaredMethod("listMyTasks", Long.class));
    }
}
