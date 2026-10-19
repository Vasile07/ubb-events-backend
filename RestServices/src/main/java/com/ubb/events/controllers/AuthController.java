package com.ubb.events.controllers;

import com.ubb.events.UserService;
import com.ubb.events.dto.RegisterUserRequest;
import com.ubb.events.dto.UserResponse;
import com.ubb.events.dto.LoginRequest;
import com.ubb.events.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a user account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already used")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = userService.register(request.getFirstName(), request.getLastName(),
                request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Sign in")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT issued"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request.getEmail(), request.getPassword()));
    }
}
