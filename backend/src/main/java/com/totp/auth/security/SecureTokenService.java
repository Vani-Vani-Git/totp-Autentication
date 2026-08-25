package com.totp.auth.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class SecureTokenService {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates cryptographically secure random bytes.
     */
    public byte[] generateRandomBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException(
                    "Random byte length must be greater than zero"
            );
        }

        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates a URL-safe random token.
     */
    public String generateToken(int byteLength) {
        byte[] bytes = generateRandomBytes(byteLength);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /**
     * Generates a random UUID.
     */
    public UUID generateUuid() {
        return UUID.randomUUID();
    }
}