package com.totp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "User ID is required")
        @Size(
                min = 3,
                max = 100,
                message = "User ID must be between 3 and 100 characters"
        )
        String userId,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must be between 8 and 72 characters"
        )
        String password
) {
}