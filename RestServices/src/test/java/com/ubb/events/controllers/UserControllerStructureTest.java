package com.ubb.events.controllers;

import com.ubb.events.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerStructureTest {
    @Test
    void retainsUsersRootMappingAndProfileHandler() throws Exception {
        assertTrue(UserController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/users", UserController.class.getAnnotation(RequestMapping.class).value()[0]);
        var profile = UserController.class.getDeclaredMethod("profile", java.security.Principal.class);
        assertEquals("/me", profile.getAnnotation(GetMapping.class).value()[0]);
        var updateProfile = UserController.class.getDeclaredMethod(
                "updateProfile", java.security.Principal.class,
                com.ubb.events.dto.UpdateUserProfileRequest.class);
        assertEquals("/me", updateProfile.getAnnotation(PutMapping.class).value()[0]);
        Constructor<?> constructor = UserController.class.getDeclaredConstructor(UserService.class);
        assertNotNull(constructor);
    }
}
