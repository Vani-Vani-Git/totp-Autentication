package com.totp.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpProvisioningServiceTest {

    private final SecureTokenService secureTokenService =
            new SecureTokenService();

    private final Base32Codec base32Codec =
            new Base32Codec();

    private final TotpSecretGenerator secretGenerator =
            new TotpSecretGenerator(
                    secureTokenService,
                    base32Codec
            );

    private final TotpProvisioningService service =
            new TotpProvisioningService(
                    secretGenerator
            );

    @Test
    void shouldCreateProvisioningData() {

        String account =
                "user@example.com";

        TotpProvisioningService.ProvisioningData data =
                service.createProvisioningData(account);

        assertEquals(
                account,
                data.accountName()
        );

        assertEquals(
                "TOTP Authentication",
                data.issuer()
        );

        assertTrue(
                data.secret().matches("[A-Z2-7]+")
        );

        assertTrue(
                data.provisioningUri()
                        .startsWith("otpauth://totp/")
        );

        assertTrue(
                data.provisioningUri()
                        .contains("algorithm=SHA1")
        );

        assertTrue(
                data.provisioningUri()
                        .contains("digits=6")
        );

        assertTrue(
                data.provisioningUri()
                        .contains("period=30")
        );
    }
}