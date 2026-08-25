package com.totp.auth.dto;

public record TotpEnrollmentResponse(
        String secret,
        String otpauthUri,
        String issuer,
        String accountName,
        int digits,
        int periodSeconds,
        String algorithm
) {
}