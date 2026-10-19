package com.ubb.events.controllers;

import com.ubb.events.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerStructureTest {
    @Test
    void retainsUsersRootMappingAndServiceDependencyWithoutHandlers() throws Exception {
        assertTrue(UserController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/users", UserController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals(0, UserController.class.getDeclaredMethods().length);
        Constructor<?> constructor = UserController.class.getDeclaredConstructor(UserService.class);
        assertNotNull(constructor);
    }
}
