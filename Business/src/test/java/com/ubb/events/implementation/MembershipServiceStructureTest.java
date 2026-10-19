package com.ubb.events.implementation;

import com.ubb.events.MembershipService;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class MembershipServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndHasNoOperations() {
        assertTrue(MembershipServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(MembershipService.class.isAssignableFrom(MembershipServiceImpl.class));
        assertEquals(0, MembershipService.class.getDeclaredMethods().length);
        assertEquals(0, MembershipServiceImpl.class.getDeclaredMethods().length);
    }
}
