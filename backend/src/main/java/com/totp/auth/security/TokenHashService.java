package com.totp.auth.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class TokenHashService {

    public byte[] hash(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Token must not be blank"
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }
}