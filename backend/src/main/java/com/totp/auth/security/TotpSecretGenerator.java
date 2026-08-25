package com.totp.auth.security;

import org.springframework.stereotype.Service;

@Service
public class TotpSecretGenerator {

    private static final int SECRET_LENGTH_BYTES = 20;

    private final SecureTokenService secureTokenService;
    private final Base32Codec base32Codec;

    public TotpSecretGenerator(
            SecureTokenService secureTokenService,
            Base32Codec base32Codec
    ) {
        this.secureTokenService = secureTokenService;
        this.base32Codec = base32Codec;
    }

    public String generateSecret() {
        byte[] randomBytes =
                secureTokenService.generateRandomBytes(
                        SECRET_LENGTH_BYTES
                );

        return base32Codec.encode(randomBytes);
    }
}