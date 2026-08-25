package com.totp.auth.dto;

public record LoginResponse(
        String status,
        String message,
        boolean totpRequired,
        String accessToken,
        String refreshToken,
        String tempAuthSessionId,
        Long tempAuthSessionExpiresInSeconds,
        Long expiresIn
) {

    public LoginResponse(
            String status,
            String message,
            boolean totpRequired,
            String accessToken,
            String refreshToken
    ) {
        this(
                status,
                message,
                totpRequired,
                accessToken,
                refreshToken,
                null,
                null,
                null
        );
    }
}