package com.ubb.events.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterUserRequest {
    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    @Pattern(regexp = ".*[0-9].*", message = "password must contain a number")
    @Pattern(regexp = ".*[A-Z].*", message = "password must contain an uppercase letter")
    @Pattern(regexp = ".*[a-z].*", message = "password must contain a lowercase letter")
    @Pattern(regexp = ".*[^A-Za-z0-9].*", message = "password must contain a special character")
    private String password;
}
