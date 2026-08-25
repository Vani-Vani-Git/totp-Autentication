package com.totp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordRecoveryVerificationRequest(

        @NotBlank(message = "User ID or email is required")
        String identifier,

        @NotBlank(message = "Verification code is required")
        @Pattern(
                regexp = "\\d{6}",
                message = "Verification code must contain 6 digits"
        )
        String code

) {
}