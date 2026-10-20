package com.ubb.events.controllers;

import com.ubb.events.UserService;
import com.ubb.events.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "View the authenticated user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required or token invalid"),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found")
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> profile(java.security.Principal principal) {
        Long userId = ((Number) ((org.springframework.security.core.Authentication) principal).getPrincipal()).longValue();
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}
