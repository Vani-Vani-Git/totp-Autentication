package com.totp.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordRecoveryRequest(

        @NotBlank(message = "User ID or email is required")
        String identifier

) {
}