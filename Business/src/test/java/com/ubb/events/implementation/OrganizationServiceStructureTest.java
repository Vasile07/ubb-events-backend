package com.ubb.events.implementation;

import com.ubb.events.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationServiceStructureTest {
    @Test
    void serviceInterfaceIsEmpty() {
        assertEquals(0, OrganizationService.class.getDeclaredMethods().length);
    }

    @Test
    void implementationIsAnnotatedAndImplementsServiceWithoutOperations() {
        assertTrue(OrganizationServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(OrganizationService.class.isAssignableFrom(OrganizationServiceImpl.class));
        assertEquals(0, OrganizationServiceImpl.class.getDeclaredMethods().length);
    }
}
