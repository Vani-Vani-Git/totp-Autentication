package com.totp.auth.dto;

public record PasswordResetRequest(
        String identifier
) {
}