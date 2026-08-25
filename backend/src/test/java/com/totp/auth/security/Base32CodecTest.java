package com.totp.auth.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base32CodecTest {

    private final Base32Codec codec = new Base32Codec();

    @Test
    void shouldDecodeBase32Value() {
        byte[] result = codec.decode("JBSWY3DP");

        assertArrayEquals(
                "Hello".getBytes(StandardCharsets.UTF_8),
                result
        );
    }

    @Test
    void shouldIgnorePadding() {
        byte[] withoutPadding =
                codec.decode("JBSWY3DPEHPK3PXP");

        byte[] withPadding =
                codec.decode("JBSWY3DPEHPK3PXP====");

        assertArrayEquals(withoutPadding, withPadding);
    }

    @Test
    void shouldRejectInvalidCharacter() {
        assertThrows(
                IllegalArgumentException.class,
                () -> codec.decode("JBSWY3DPEHPK3PX0")
        );
    }

    @Test
    void shouldRejectEmptyValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> codec.decode("")
        );
    }
}