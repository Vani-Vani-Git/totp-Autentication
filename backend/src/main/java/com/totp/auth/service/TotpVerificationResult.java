package com.totp.auth.service;

public record TotpVerificationResult(
        Status status,
        AuthenticatedSessionService.TokenPair tokenPair,
        long expiresIn
) {

    public enum Status {
        SUCCESS,
        INVALID_OTP,
        EXPIRED_OTP,
        LOCKED,
        REPLAY
    }

    public static TotpVerificationResult success(
            AuthenticatedSessionService.TokenPair tokenPair,
            long expiresIn
    ) {
        return new TotpVerificationResult(
                Status.SUCCESS,
                tokenPair,
                expiresIn
        );
    }

    public static TotpVerificationResult failure(
            Status status
    ) {
        return new TotpVerificationResult(
                status,
                null,
                0
        );
    }
}