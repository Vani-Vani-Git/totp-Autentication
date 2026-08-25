package com.totp.auth.service;

import com.totp.auth.entity.PasswordRecoveryCode;
import com.totp.auth.entity.User;
import com.totp.auth.repository.PasswordRecoveryCodeRepository;
import com.totp.auth.repository.UserRepository;
import com.totp.auth.security.SecureTokenService;
import com.totp.auth.security.TokenHashService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.MessageDigest;

@Service
public class PasswordRecoveryService {

    private static final String PENDING = "PENDING";

    private final UserRepository userRepository;
    private final PasswordRecoveryCodeRepository
            passwordRecoveryCodeRepository;
    private final SecureTokenService secureTokenService;
    private final TokenHashService tokenHashService;
    private final PasswordRecoveryEmailService
            passwordRecoveryEmailService;
    private final PasswordResetService passwordResetService;

    private final long codeTtlMinutes;

    public PasswordRecoveryService(
            UserRepository userRepository,
            PasswordRecoveryCodeRepository passwordRecoveryCodeRepository,
            SecureTokenService secureTokenService,
            TokenHashService tokenHashService,
            PasswordRecoveryEmailService passwordRecoveryEmailService,
            PasswordResetService passwordResetService,
            @Value(
                    "${app.security.password-recovery-code-ttl-minutes:10}"
            )
            long codeTtlMinutes
    ) {
        this.userRepository = userRepository;
        this.passwordRecoveryCodeRepository =
                passwordRecoveryCodeRepository;
        this.secureTokenService = secureTokenService;
        this.tokenHashService = tokenHashService;
        this.passwordRecoveryEmailService =
                passwordRecoveryEmailService;
        this.passwordResetService =
                passwordResetService;
        this.codeTtlMinutes = codeTtlMinutes;
    }

    @Transactional
    public void sendRecoveryCode(String identifier) {

        String normalizedIdentifier =
                identifier.trim();

        User user =
                userRepository
                        .findByUserIdOrEmail(
                                normalizedIdentifier,
                                normalizedIdentifier.toLowerCase()
                        )
                        .orElse(null);

        /*
         * Do not reveal whether the account exists.
         */
        if (user == null) {
            return;
        }

        /*
         * Email recovery is only available
         * when TOTP has not been enabled.
         */
        if (user.isTotpEnabled()) {
            return;
        }

        /*
         * Invalidate the previous pending code.
         */
        passwordRecoveryCodeRepository
                .findTopByUserAndStatusOrderByCreatedAtDesc(
                        user,
                        PENDING
                )
                .ifPresent(existing -> {
                    existing.setStatus("REPLACED");

                    passwordRecoveryCodeRepository.save(
                            existing
                    );
                });

        /*
         * Generate a cryptographically secure
         * 6-digit verification code.
         */
        String code =
                generateSixDigitCode();

        byte[] codeHash =
                tokenHashService.hash(code);

        PasswordRecoveryCode recoveryCode =
                new PasswordRecoveryCode();

        recoveryCode.setUser(user);

        recoveryCode.setCodeHash(
                codeHash
        );

        recoveryCode.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(codeTtlMinutes)
        );

        recoveryCode.setFailedAttempts(0);

        recoveryCode.setStatus(
                PENDING
        );

        passwordRecoveryCodeRepository.save(
                recoveryCode
        );

        /*
         * Send the plaintext code only through
         * the registered email address.
         */
        passwordRecoveryEmailService.sendRecoveryCode(
                user.getEmail(),
                code
        );
    }

    @Transactional
    public PasswordRecoveryVerificationResult verifyRecoveryCode(
            String identifier,
            String code
    ) {

        String normalizedIdentifier =
                identifier.trim();

        User user =
                userRepository
                        .findByUserIdOrEmail(
                                normalizedIdentifier,
                                normalizedIdentifier.toLowerCase()
                        )
                        .orElse(null);

        if (user == null) {

            return new PasswordRecoveryVerificationResult(
                    PasswordRecoveryVerificationResult.Status.NOT_FOUND,
                    null
            );
        }

        /*
         * Users with TOTP enabled must use
         * the authenticator-based recovery flow.
         */
        if (user.isTotpEnabled()) {

            return new PasswordRecoveryVerificationResult(
                    PasswordRecoveryVerificationResult.Status.TOTP_ENABLED,
                    null
            );
        }

        PasswordRecoveryCode recoveryCode =
                passwordRecoveryCodeRepository
                        .findTopByUserAndStatusOrderByCreatedAtDesc(
                                user,
                                PENDING
                        )
                        .orElse(null);

        if (recoveryCode == null) {

            return new PasswordRecoveryVerificationResult(
                    PasswordRecoveryVerificationResult.Status.NOT_FOUND,
                    null
            );
        }

        /*
         * Check expiration before checking
         * the supplied code.
         */
        if (recoveryCode.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            recoveryCode.setStatus(
                    "EXPIRED"
            );

            passwordRecoveryCodeRepository.save(
                    recoveryCode
            );

            return new PasswordRecoveryVerificationResult(
                    PasswordRecoveryVerificationResult.Status.EXPIRED,
                    null
            );
        }

        /*
         * Maximum of five failed attempts.
         */
        if (recoveryCode.getFailedAttempts() >= 5) {

            recoveryCode.setStatus(
                    "LOCKED"
            );

            passwordRecoveryCodeRepository.save(
                    recoveryCode
            );

            return new PasswordRecoveryVerificationResult(
                    PasswordRecoveryVerificationResult.Status.LOCKED,
                    null
            );
        }

        byte[] suppliedCodeHash =
                tokenHashService.hash(code);

        boolean codeMatches =
                MessageDigest.isEqual(
                        suppliedCodeHash,
                        recoveryCode.getCodeHash()
                );

        if (!codeMatches) {

            int attempts =
                    recoveryCode.getFailedAttempts() + 1;

            recoveryCode.setFailedAttempts(
                    attempts
            );

            if (attempts >= 5) {

                recoveryCode.setStatus(
                        "LOCKED"
                );
            }

            passwordRecoveryCodeRepository.save(
                    recoveryCode
            );

            PasswordRecoveryVerificationResult.Status status =
                    attempts >= 5
                            ? PasswordRecoveryVerificationResult.Status.LOCKED
                            : PasswordRecoveryVerificationResult.Status.INVALID_CODE;

            return new PasswordRecoveryVerificationResult(
                    status,
                    null
            );
        }

        /*
         * The email OTP has been successfully verified.
         */
        recoveryCode.setStatus(
                "VERIFIED"
        );

        recoveryCode.setFailedAttempts(0);

        passwordRecoveryCodeRepository.save(
                recoveryCode
        );

        /*
         * Convert the successful email verification
         * into the existing password-reset authorization.
         */
        String resetToken =
                passwordResetService
                        .createEmailVerifiedResetToken(
                                user.getUserId()
                        );

        return new PasswordRecoveryVerificationResult(
                PasswordRecoveryVerificationResult.Status.SUCCESS,
                resetToken
        );
    }

    private String generateSixDigitCode() {

        String randomToken =
                secureTokenService.generateToken(8);

        byte[] hash =
                tokenHashService.hash(
                        randomToken
                );

        long numericValue = 0;

        for (int index = 0;
             index < 4;
             index++) {

            numericValue =
                    (numericValue << 8)
                            | (hash[index] & 0xffL);
        }

        int code =
                (int) (
                        numericValue
                                % 1_000_000
                );

        return String.format(
                "%06d",
                code
        );
    }
}