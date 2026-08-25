package com.totp.auth.service;

import com.totp.auth.entity.PasswordResetToken;
import com.totp.auth.entity.TotpSecret;
import com.totp.auth.entity.User;
import com.totp.auth.repository.PasswordResetTokenRepository;
import com.totp.auth.repository.TotpSecretRepository;
import com.totp.auth.repository.UserRepository;
import com.totp.auth.security.SecureTokenService;
import com.totp.auth.security.TokenHashService;
import com.totp.auth.security.TotpSecretEncryptionService;
import com.totp.auth.security.TotpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final long RESET_TOKEN_VALID_MINUTES = 10;
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository
            passwordResetTokenRepository;
    private final TotpSecretRepository totpSecretRepository;
    private final SecureTokenService secureTokenService;
    private final TokenHashService tokenHashService;
    private final TotpSecretEncryptionService
            totpSecretEncryptionService;
    private final TotpService totpService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository
                    passwordResetTokenRepository,
            TotpSecretRepository totpSecretRepository,
            SecureTokenService secureTokenService,
            TokenHashService tokenHashService,
            TotpSecretEncryptionService
                    totpSecretEncryptionService,
            TotpService totpService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;
        this.totpSecretRepository =
                totpSecretRepository;
        this.secureTokenService = secureTokenService;
        this.tokenHashService = tokenHashService;
        this.totpSecretEncryptionService =
                totpSecretEncryptionService;
        this.totpService = totpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createResetToken(
            String identifier
    ) {
        if (identifier == null
                || identifier.isBlank()) {
            throw new IllegalArgumentException(
                    "Identifier is required"
            );
        }

        String normalized =
                identifier.trim().toLowerCase();

        Optional<User> user =
                userRepository.findByUserIdOrEmail(
                        normalized,
                        normalized
                );

        /*
         * Do not reveal whether an account exists.
         *
         * The controller will return the same generic
         * response regardless of whether the user exists.
         */
        if (user.isEmpty()) {
            return null;
        }

        User account = user.get();

        if (!account.isTotpEnabled()) {
            return null;
        }

        Optional<TotpSecret> totpSecret =
                totpSecretRepository.findByUser(account);

        if (totpSecret.isEmpty()) {
            return null;
        }

        String rawToken =
                secureTokenService.generateToken(32);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUser(account);
        resetToken.setTokenHash(
                tokenHashService.hash(rawToken)
        );
        resetToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(
                        RESET_TOKEN_VALID_MINUTES
                )
        );
        resetToken.setFailedAttempts(0);
        resetToken.setStatus(
                PasswordResetToken.Status.PENDING
        );

        passwordResetTokenRepository.save(
                resetToken
        );

        return rawToken;
    }

    @Transactional
    public void verifyTotp(
            String rawToken,
            String suppliedCode
    ) {
        PasswordResetToken resetToken =
                findValidResetToken(rawToken);

        if (suppliedCode == null
                || !suppliedCode.trim()
                .matches("\\d{6}")) {

            registerFailedAttempt(resetToken);

            throw new IllegalArgumentException(
                    "Invalid TOTP code"
            );
        }

        TotpSecret totpSecret =
                totpSecretRepository
                        .findByUser(
                                resetToken.getUser()
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "TOTP enrollment was not found"
                                )
                        );

        String secret =
                totpSecretEncryptionService.decrypt(
                        totpSecret.getEncryptedSecret()
                );

        boolean valid =
                totpService.verifyCode(
                        secret,
                        suppliedCode.trim(),
                        System.currentTimeMillis()
                                / 1000L
                );

        if (!valid) {
            registerFailedAttempt(resetToken);

            throw new IllegalArgumentException(
                    "Invalid TOTP code"
            );
        }

        resetToken.setStatus(
                PasswordResetToken.Status.VERIFIED
        );

        resetToken.setFailedAttempts(0);

        passwordResetTokenRepository.save(
                resetToken
        );
    }

    @Transactional
    public String createEmailVerifiedResetToken(
            String identifier
    ) {
        if (identifier == null
                || identifier.isBlank()) {
            throw new IllegalArgumentException(
                    "Identifier is required"
            );
        }

        String normalized =
                identifier.trim();

        Optional<User> user =
                userRepository.findByUserIdOrEmail(
                        normalized,
                        normalized.toLowerCase()
                );

        if (user.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid password recovery request"
            );
        }

        User account = user.get();

        /*
         * Email recovery is only available when
         * TOTP is not enabled.
         */
        if (account.isTotpEnabled()) {
            throw new IllegalArgumentException(
                    "TOTP recovery is required"
            );
        }

        String rawToken =
                secureTokenService.generateToken(32);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUser(account);

        resetToken.setTokenHash(
                tokenHashService.hash(rawToken)
        );

        resetToken.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(
                                RESET_TOKEN_VALID_MINUTES
                        )
        );

        resetToken.setFailedAttempts(0);

        resetToken.setStatus(
                PasswordResetToken.Status.VERIFIED
        );

        passwordResetTokenRepository.save(
                resetToken
        );

        return rawToken;
    }

    @Transactional
    public void completeReset(
            String rawToken,
            String newPassword
    ) {
        if (newPassword == null
                || newPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "New password is required"
            );
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters"
            );
        }

        PasswordResetToken resetToken =
                findValidResetToken(rawToken);

        if (resetToken.getStatus()
                != PasswordResetToken.Status.VERIFIED) {

            throw new IllegalArgumentException(
                    "Password reset verification is required"
            );
        }

        User user = resetToken.getUser();

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        resetToken.setStatus(
                PasswordResetToken.Status.USED
        );

        passwordResetTokenRepository.save(
                resetToken
        );
    }

    private PasswordResetToken findValidResetToken(
            String rawToken
    ) {
        if (rawToken == null
                || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Reset token is required"
            );
        }

        byte[] tokenHash =
                tokenHashService.hash(rawToken);

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Invalid password reset request"
                                )
                        );

        if (resetToken.getExpiresAt() == null
                || !resetToken.getExpiresAt()
                .isAfter(LocalDateTime.now())) {

            resetToken.setStatus(
                    PasswordResetToken.Status.EXPIRED
            );

            passwordResetTokenRepository.save(
                    resetToken
            );

            throw new IllegalArgumentException(
                    "Password reset request has expired"
            );
        }

        if (resetToken.getStatus()
                == PasswordResetToken.Status.LOCKED) {

            throw new IllegalArgumentException(
                    "Password reset request is locked"
            );
        }

        if (resetToken.getStatus()
                == PasswordResetToken.Status.USED) {

            throw new IllegalArgumentException(
                    "Password reset request has already been used"
            );
        }

        return resetToken;
    }

    private void registerFailedAttempt(
            PasswordResetToken resetToken
    ) {
        int attempts =
                resetToken.getFailedAttempts() + 1;

        resetToken.setFailedAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            resetToken.setStatus(
                    PasswordResetToken.Status.LOCKED
            );
        }

        passwordResetTokenRepository.save(
                resetToken
        );
    }
}