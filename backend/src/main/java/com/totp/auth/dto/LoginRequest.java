package com.totp.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Login identifier is required")
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {
}