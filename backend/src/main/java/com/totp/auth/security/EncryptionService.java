package com.totp.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

    public EncryptionService(
            @Value("${app.security.encryption-key}") String base64Key
    ) {
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "TOTP_ENCRYPTION_KEY must be valid Base64",
                    exception
            );
        }

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "TOTP_ENCRYPTION_KEY must decode to exactly 32 bytes"
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        this.secureRandom = new SecureRandom();
    }

    public EncryptionResult encrypt(byte[] plaintext) {
        if (plaintext == null || plaintext.length == 0) {
            throw new IllegalArgumentException(
                    "Plaintext cannot be null or empty"
            );
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            GCMParameterSpec gcmParameterSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH_BITS,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    gcmParameterSpec
            );

            byte[] ciphertext = cipher.doFinal(plaintext);

            return new EncryptionResult(ciphertext, iv);

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to encrypt data",
                    exception
            );
        }
    }

    public byte[] decrypt(
            byte[] ciphertext,
            byte[] iv
    ) {
        if (ciphertext == null || ciphertext.length == 0) {
            throw new IllegalArgumentException(
                    "Ciphertext cannot be null or empty"
            );
        }

        if (iv == null || iv.length != IV_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid AES-GCM IV"
            );
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            GCMParameterSpec gcmParameterSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH_BITS,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    gcmParameterSpec
            );

            return cipher.doFinal(ciphertext);

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to decrypt data",
                    exception
            );
        }
    }

    public record EncryptionResult(
            byte[] ciphertext,
            byte[] iv
    ) {
    }
}