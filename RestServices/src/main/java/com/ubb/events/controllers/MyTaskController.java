package com.ubb.events.controllers;

import com.ubb.events.EventTaskService;
import com.ubb.events.dto.MyTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class MyTaskController {
    private final EventTaskService eventTaskService;

    public MyTaskController(EventTaskService eventTaskService) {
        this.eventTaskService = eventTaskService;
    }

    @GetMapping("/me/tasks")
    @Operation(summary = "View the tasks assigned to the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned tasks returned with their event and organization"),
            @ApiResponse(responseCode = "401", description = "Authentication required or token invalid")
    })
    public ResponseEntity<List<MyTaskResponse>> listMyTasks(Principal principal) {
        Long userId = ((Number) ((Authentication) principal).getPrincipal()).longValue();
        return ResponseEntity.ok(eventTaskService.listMyTasks(userId));
    }
}
