package com.totp.auth.service;

import com.totp.auth.entity.TotpSecret;
import com.totp.auth.entity.User;
import com.totp.auth.repository.TotpSecretRepository;
import com.totp.auth.security.TotpSecretEncryptionService;
import com.totp.auth.security.TotpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.totp.auth.repository.UserRepository;

@Service
public class TotpConfirmationService {

    private final TotpSecretRepository totpSecretRepository;
    private final TotpSecretEncryptionService encryptionService;
    private final TotpService totpService;
    private final UserRepository userRepository;

    public TotpConfirmationService(
            TotpSecretRepository totpSecretRepository,
            TotpSecretEncryptionService encryptionService,
            TotpService totpService,
            UserRepository userRepository
    ) {
        this.totpSecretRepository =
                totpSecretRepository;
        this.encryptionService =
                encryptionService;
        this.totpService =
                totpService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void confirm(
            User user,
            String suppliedCode
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

        if (suppliedCode == null
                || suppliedCode.isBlank()) {
            throw new IllegalArgumentException(
                    "TOTP code is required"
            );
        }

        String normalizedCode =
                suppliedCode.trim();

        if (!normalizedCode.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "TOTP code must contain exactly 6 digits"
            );
        }

        TotpSecret totpSecret =
                totpSecretRepository
                        .findByUser(user)
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "TOTP enrollment was not found"
                                )
                        );

        String secret =
                encryptionService.decrypt(
                        totpSecret.getEncryptedSecret()
                );

        boolean valid =
                totpService.verifyCode(
                        secret,
                        normalizedCode,
                        System.currentTimeMillis() / 1000L
                );

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid TOTP code"
            );
        }

        user.setTotpEnabled(true);

        userRepository.save(user);
    }
}