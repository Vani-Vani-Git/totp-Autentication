package com.totp.auth.dto;

public record PasswordResetCompleteRequest(
        String resetToken,
        String newPassword
) {
}