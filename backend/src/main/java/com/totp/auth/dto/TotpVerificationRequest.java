package com.totp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TotpVerificationRequest(

        @NotBlank(message = "Temporary authentication session is required")
        String tempAuthSessionId,

        @NotBlank(message = "OTP is required")
        @Pattern(
                regexp = "\\d{6}",
                message = "OTP must contain exactly 6 digits"
        )
        String otp
) {
}