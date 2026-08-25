package com.totp.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenHashServiceTest {

    private final TokenHashService service =
            new TokenHashService();

    @Test
    void shouldGenerateSha256Hash() {

        byte[] hash =
                service.hash("test-token");

        assertNotNull(hash);
        assertEquals(32, hash.length);
    }

    @Test
    void sameTokenShouldProduceSameHash() {

        byte[] first =
                service.hash("same-token");

        byte[] second =
                service.hash("same-token");

        assertArrayEquals(first, second);
    }

    @Test
    void differentTokensShouldProduceDifferentHashes() {

        byte[] first =
                service.hash("token-one");

        byte[] second =
                service.hash("token-two");

        assertFalse(
                java.util.Arrays.equals(
                        first,
                        second
                )
        );
    }

    @Test
    void shouldRejectNullToken() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.hash(null)
        );
    }

    @Test
    void shouldRejectBlankToken() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.hash("   ")
        );
    }
}