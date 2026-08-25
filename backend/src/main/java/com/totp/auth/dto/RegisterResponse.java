package com.totp.auth.dto;

public record RegisterResponse(
        Long id,
        String userId,
        String email,
        String status,
        boolean totpEnabled
) {
}