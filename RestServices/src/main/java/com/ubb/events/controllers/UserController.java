package com.ubb.events.controllers;

import com.ubb.events.UserService;
import com.ubb.events.dto.UserProfileResponse;
import com.ubb.events.dto.UpdateUserProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

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
        Long userId = principalUserId(principal);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @Operation(summary = "Update the authenticated user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required or token invalid"),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found")
    })
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            java.security.Principal principal,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principalUserId(principal), request));
    }

    private Long principalUserId(java.security.Principal principal) {
        return ((Number) ((org.springframework.security.core.Authentication) principal).getPrincipal()).longValue();
    }
}
