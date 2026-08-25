package com.totp.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class TotpSecretEncryptionServiceTest {

    private TotpSecretEncryptionService service;

    @BeforeEach
    void setUp() {

        byte[] key = new byte[32];

        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) (i + 1);
        }

        String base64Key =
                Base64.getEncoder()
                        .encodeToString(key);

        service =
                new TotpSecretEncryptionService(
                        base64Key
                );

        ReflectionTestUtils.invokeMethod(
                service,
                "initialize"
        );
    }

    @Test
    void shouldEncryptAndDecryptSecret() {

        String originalSecret =
                "JBSWY3DPEHPK3PXP";

        String encrypted =
                service.encrypt(originalSecret);

        assertNotNull(encrypted);

        assertNotEquals(
                originalSecret,
                encrypted
        );

        String decrypted =
                service.decrypt(encrypted);

        assertEquals(
                originalSecret,
                decrypted
        );
    }

    @Test
    void encryptingSameSecretTwiceShouldProduceDifferentCiphertext() {

        String secret =
                "JBSWY3DPEHPK3PXP";

        String first =
                service.encrypt(secret);

        String second =
                service.encrypt(secret);

        assertNotEquals(
                first,
                second
        );

        assertEquals(
                secret,
                service.decrypt(first)
        );

        assertEquals(
                secret,
                service.decrypt(second)
        );
    }

    @Test
    void shouldRejectBlankPlainText() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.encrypt("   ")
        );
    }

    @Test
    void shouldRejectNullPlainText() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.encrypt(null)
        );
    }

    @Test
    void shouldRejectBlankEncryptedText() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.decrypt("   ")
        );
    }

    @Test
    void shouldRejectNullEncryptedText() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.decrypt(null)
        );
    }

    @Test
    void shouldRejectTamperedCiphertext() {

        String encrypted =
                service.encrypt(
                        "JBSWY3DPEHPK3PXP"
                );

        byte[] bytes =
                Base64.getDecoder()
                        .decode(encrypted);

        bytes[bytes.length - 1] ^= 1;

        String tampered =
                Base64.getEncoder()
                        .encodeToString(bytes);

        assertThrows(
                IllegalStateException.class,
                () -> service.decrypt(tampered)
        );
    }

    @Test
    void shouldRejectInvalidEncryptionKeyLength() {

        byte[] shortKey = new byte[16];

        String invalidKey =
                Base64.getEncoder()
                        .encodeToString(shortKey);

        TotpSecretEncryptionService invalidService =
                new TotpSecretEncryptionService(
                        invalidKey
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> ReflectionTestUtils.invokeMethod(
                                invalidService,
                                "initialize"
                        )
                );

        assertEquals(
                "TOTP encryption key must decode to 32 bytes",
                exception.getMessage()
        );
    }
}