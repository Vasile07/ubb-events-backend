package com.ubb.events.implementation;

import com.ubb.events.MembershipService;
import com.ubb.events.dto.MembershipRequestResponse;
import com.ubb.events.domain.MembershipStatus;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

class MembershipServiceStructureTest {
    @Test
    void implementationIsAnnotatedAndExposesMembershipRequestOperation() {
        assertTrue(MembershipServiceImpl.class.isAnnotationPresent(Service.class));
        assertTrue(MembershipService.class.isAssignableFrom(MembershipServiceImpl.class));
        assertNotNull(findMethod(MembershipService.class, "listMembershipRequests", Long.class, MembershipStatus.class));
        assertNotNull(findMethod(MembershipServiceImpl.class, "listMembershipRequests", Long.class, MembershipStatus.class));
    }

    private java.lang.reflect.Method findMethod(Class<?> type, String name, Class<?>... parameters) {
        try { return type.getDeclaredMethod(name, parameters); }
        catch (NoSuchMethodException exception) { return null; }
    }
}
