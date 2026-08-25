package com.totp.auth.security;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TotpProvisioningService {

    private static final String DEFAULT_ISSUER =
            "TOTP Authentication";

    private final TotpSecretGenerator secretGenerator;

    public TotpProvisioningService(
            TotpSecretGenerator secretGenerator
    ) {
        this.secretGenerator = secretGenerator;
    }

    public ProvisioningData createProvisioningData(
            String accountName
    ) {
        if (accountName == null || accountName.isBlank()) {
            throw new IllegalArgumentException(
                    "Account name cannot be empty"
            );
        }

        String secret = secretGenerator.generateSecret();

        String encodedIssuer =
                encode(DEFAULT_ISSUER);

        String encodedAccount =
                encode(accountName);

        String label =
                encodedIssuer + "%3A" + encodedAccount;

        String uri =
                "otpauth://totp/"
                        + label
                        + "?secret="
                        + secret
                        + "&issuer="
                        + encodedIssuer
                        + "&algorithm=SHA1"
                        + "&digits=6"
                        + "&period=30";

        return new ProvisioningData(
                secret,
                uri,
                DEFAULT_ISSUER,
                accountName
        );
    }

    private String encode(String value) {
        return URLEncoder
                .encode(
                        value,
                        StandardCharsets.UTF_8
                )
                .replace("+", "%20");
    }

    public record ProvisioningData(
            String secret,
            String provisioningUri,
            String issuer,
            String accountName
    ) {
    }
}