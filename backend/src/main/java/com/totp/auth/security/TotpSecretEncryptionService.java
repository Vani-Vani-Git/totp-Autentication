package com.totp.auth.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TotpSecretEncryptionService {

    private static final String TRANSFORMATION =
            "AES/GCM/NoPadding";

    private static final int IV_LENGTH_BYTES = 12;

    private static final int TAG_LENGTH_BITS = 128;

    private final String configuredKey;

    private SecretKeySpec encryptionKey;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public TotpSecretEncryptionService(
            @Value("${app.security.encryption-key}")
            String configuredKey
    ) {
        this.configuredKey = configuredKey;
    }

    @PostConstruct
    void initialize() {

        if (configuredKey == null
                || configuredKey.isBlank()) {

            throw new IllegalStateException(
                    "TOTP encryption key is not configured"
            );
        }

        byte[] keyBytes;

        try {
            keyBytes =
                    Base64.getDecoder()
                            .decode(configuredKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "TOTP encryption key must be valid Base64",
                    exception
            );
        }

        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "TOTP encryption key must decode to 32 bytes"
            );
        }

        encryptionKey =
                new SecretKeySpec(
                        keyBytes,
                        "AES"
                );
    }

    public String encrypt(String plainText) {

        if (plainText == null
                || plainText.isBlank()) {

            throw new IllegalArgumentException(
                    "Plain text must not be blank"
            );
        }

        try {
            byte[] iv =
                    new byte[IV_LENGTH_BYTES];

            secureRandom.nextBytes(iv);

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    encryptionKey,
                    parameterSpec
            );

            byte[] cipherText =
                    cipher.doFinal(
                            plainText.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            byte[] combined =
                    new byte[
                            iv.length
                                    + cipherText.length
                            ];

            System.arraycopy(
                    iv,
                    0,
                    combined,
                    0,
                    iv.length
            );

            System.arraycopy(
                    cipherText,
                    0,
                    combined,
                    iv.length,
                    cipherText.length
            );

            return Base64.getEncoder()
                    .encodeToString(combined);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to encrypt TOTP secret",
                    exception
            );
        }
    }

    public String decrypt(String encryptedText) {

        if (encryptedText == null
                || encryptedText.isBlank()) {

            throw new IllegalArgumentException(
                    "Encrypted text must not be blank"
            );
        }

        try {
            byte[] combined =
                    Base64.getDecoder()
                            .decode(encryptedText);

            if (combined.length
                    <= IV_LENGTH_BYTES) {

                throw new IllegalArgumentException(
                        "Invalid encrypted value"
                );
            }

            byte[] iv =
                    new byte[IV_LENGTH_BYTES];

            byte[] cipherText =
                    new byte[
                            combined.length
                                    - IV_LENGTH_BYTES
                            ];

            System.arraycopy(
                    combined,
                    0,
                    iv,
                    0,
                    IV_LENGTH_BYTES
            );

            System.arraycopy(
                    combined,
                    IV_LENGTH_BYTES,
                    cipherText,
                    0,
                    cipherText.length
            );

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    encryptionKey,
                    parameterSpec
            );

            byte[] plainText =
                    cipher.doFinal(cipherText);

            return new String(
                    plainText,
                    StandardCharsets.UTF_8
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to decrypt TOTP secret",
                    exception
            );
        }
    }
}