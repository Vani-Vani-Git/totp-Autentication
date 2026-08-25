package com.totp.auth.service;

public record PasswordRecoveryVerificationResult(
        Status status,
        String resetToken
) {

    public enum Status {
        SUCCESS,
        INVALID_CODE,
        EXPIRED,
        LOCKED,
        NOT_FOUND,
        TOTP_ENABLED
    }
}