package com.totp.auth.dto;

public record PasswordResetResponse(
        String status,
        String message,
        String resetToken
) {
}
