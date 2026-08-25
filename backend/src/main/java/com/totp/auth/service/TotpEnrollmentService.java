package com.totp.auth.service;

import com.totp.auth.dto.TotpEnrollmentResponse;
import com.totp.auth.entity.TotpSecret;
import com.totp.auth.entity.User;
import com.totp.auth.repository.TotpSecretRepository;
import com.totp.auth.security.TotpSecretEncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.totp.auth.security.Base32Codec;

@Service
public class TotpEnrollmentService {

    private static final String ISSUER =
            "TOTP Authentication";

    private static final String ALGORITHM =
            "SHA1";

    private static final int DIGITS =
            6;

    private static final int PERIOD_SECONDS =
            30;

    private final TotpSecretRepository totpSecretRepository;
    private final TotpSecretEncryptionService encryptionService;
    private final Base32Codec base32Codec;

    public TotpEnrollmentService(
            TotpSecretRepository totpSecretRepository,
            TotpSecretEncryptionService encryptionService,
            Base32Codec base32Codec
    ) {
        this.totpSecretRepository =
                totpSecretRepository;
        this.encryptionService =
                encryptionService;
        this.base32Codec =
                base32Codec;
    }

    @Transactional
    public TotpEnrollmentResponse enroll(
            User user
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required"
            );
        }

        if (user.isTotpEnabled()) {
            throw new IllegalArgumentException(
                    "TOTP is already enabled"
            );
        }

        if (totpSecretRepository.existsByUser(user)) {
            throw new IllegalArgumentException(
                    "TOTP enrollment already exists"
            );
        }

        String secret =
                generateSecret();

        String encryptedSecret =
                encryptionService.encrypt(
                        secret
                );

        TotpSecret totpSecret =
                new TotpSecret();

        totpSecret.setUser(user);
        totpSecret.setEncryptedSecret(
                encryptedSecret
        );
        totpSecret.setAlgorithm(
                ALGORITHM
        );
        totpSecret.setDigits(
                DIGITS
        );
        totpSecret.setPeriodSeconds(
                PERIOD_SECONDS
        );

        totpSecretRepository.save(
                totpSecret
        );

        String accountName =
                user.getEmail();

        String otpauthUri =
                buildOtpAuthUri(
                        secret,
                        accountName
                );

        return new TotpEnrollmentResponse(
                secret,
                otpauthUri,
                ISSUER,
                accountName,
                DIGITS,
                PERIOD_SECONDS,
                ALGORITHM
        );
    }

    private String generateSecret() {

        byte[] secretBytes = new byte[20];

        java.security.SecureRandom secureRandom =
                new java.security.SecureRandom();

        secureRandom.nextBytes(secretBytes);

        return base32Codec.encode(secretBytes);
    }

    private String buildOtpAuthUri(
            String secret,
            String accountName
    ) {

        String encodedIssuer =
                urlEncode(ISSUER);

        String encodedAccount =
                urlEncode(accountName);

        return "otpauth://totp/"
                + encodedIssuer
                + ":"
                + encodedAccount
                + "?secret="
                + secret
                + "&issuer="
                + encodedIssuer
                + "&algorithm="
                + ALGORITHM
                + "&digits="
                + DIGITS
                + "&period="
                + PERIOD_SECONDS;
    }

    private String urlEncode(
            String value
    ) {

        return java.net.URLEncoder
                .encode(
                        value,
                        java.nio.charset.StandardCharsets.UTF_8
                )
                .replace(
                        "+",
                        "%20"
                );
    }
}