package com.totp.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpSecretGeneratorTest {

    private final SecureTokenService secureTokenService =
            new SecureTokenService();

    private final Base32Codec base32Codec =
            new Base32Codec();

    private final TotpSecretGenerator generator =
            new TotpSecretGenerator(
                    secureTokenService,
                    base32Codec
            );

    @Test
    void shouldGenerateBase32Secret() {
        String secret = generator.generateSecret();

        assertEquals(32, secret.length());

        assertTrue(
                secret.matches("[A-Z2-7]+")
        );
    }

    @Test
    void shouldGenerateDifferentSecrets() {
        String first = generator.generateSecret();
        String second = generator.generateSecret();

        assertNotEquals(first, second);
    }

    @Test
    void generatedSecretShouldRoundTripThroughBase32() {
        String secret = generator.generateSecret();

        byte[] decoded =
                base32Codec.decode(secret);

        String encodedAgain =
                base32Codec.encode(decoded);

        assertEquals(secret, encodedAgain);
    }
}