package com.totp.auth.dto;

public record PasswordResetVerifyRequest(
        String resetToken,
        String totpCode
) {
}