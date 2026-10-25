package com.ubb.events.controllers;

import com.ubb.events.EventTaskService;
import com.ubb.events.security.NoOrgCheck;
import com.ubb.events.security.RequireOrgPermission;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;

class MyTaskControllerStructureTest {
    @Test
    void controllerIsUserScopedWithServiceDependency() throws Exception {
        assertTrue(MyTaskController.class.isAnnotationPresent(RestController.class));
        assertEquals("/api/users", MyTaskController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertNotNull(MyTaskController.class.getDeclaredConstructor(EventTaskService.class));
    }

    @Test
    void myTasksHandlerIsMappedOutsideOrganizationPath() throws Exception {
        Method method = MyTaskController.class.getDeclaredMethod("listMyTasks", Principal.class);
        assertNotNull(method.getAnnotation(GetMapping.class), "listMyTasks must be a GET handler");
        assertEquals("/me/tasks", method.getAnnotation(GetMapping.class).value()[0]);
        assertFalse(method.isAnnotationPresent(RequireOrgPermission.class));
        assertFalse(method.isAnnotationPresent(NoOrgCheck.class));
    }
}
