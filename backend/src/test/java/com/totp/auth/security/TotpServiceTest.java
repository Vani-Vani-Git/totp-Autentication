package com.totp.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotpServiceTest {

    private final Base32Codec base32Codec = new Base32Codec();

    private final TotpService totpService =
            new TotpService(base32Codec);

    @Test
    void shouldGenerateRfc6238CodeAtTime59() {

        String secret =
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = totpService.generateCode(
                secret,
                59L,
                30L,
                8
        );

        assertEquals("94287082", code);
    }

    @Test
    void shouldGenerateRfc6238CodeAtTime1111111109() {

        String secret =
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = totpService.generateCode(
                secret,
                1111111109L,
                30L,
                8
        );

        assertEquals("07081804", code);
    }

    @Test
    void shouldGenerateSixDigitCode() {

        String secret =
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        String code = totpService.generateCode(
                secret,
                59L,
                30L,
                6
        );

        assertEquals(6, code.length());
    }

    @Test
    void shouldAcceptCurrentTimeStepCode() {

        String secret =
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        long time = 59L;

        String code = totpService.generateCode(
                secret,
                time,
                30L,
                6
        );

        assertEquals(
                true,
                totpService.verifyCode(
                        secret,
                        code,
                        time
                )
        );
    }
    @Test
    void shouldAcceptAdjacentTimeStepCode() {

        String secret =
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        long currentTime = 60L;

        String previousCode = totpService.generateCode(
                secret,
                30L,
                30L,
                6
        );

        String nextCode = totpService.generateCode(
                secret,
                90L,
                30L,
                6
        );

        assertEquals(
                true,
                totpService.verifyCode(
                        secret,
                        previousCode,
                        currentTime
                )
        );

        assertEquals(
                true,
                totpService.verifyCode(
                        secret,
                        nextCode,
                        currentTime
                )
        );
    }
    @Test
    void shouldRejectInvalidCode() {

        String secret =
                "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertEquals(
                false,
                totpService.verifyCode(
                        secret,
                        "000000",
                        59L
                )
        );
    }
}