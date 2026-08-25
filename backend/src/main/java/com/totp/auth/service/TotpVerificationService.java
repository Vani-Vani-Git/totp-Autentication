package com.totp.auth.service;

import com.totp.auth.entity.TemporaryAuthSession;
import com.totp.auth.entity.TotpSecret;
import com.totp.auth.entity.User;
import com.totp.auth.repository.TemporaryAuthSessionRepository;
import com.totp.auth.repository.TotpSecretRepository;
import com.totp.auth.security.SecureTokenService;
import com.totp.auth.security.TokenHashService;
import com.totp.auth.security.TotpSecretEncryptionService;
import com.totp.auth.security.TotpService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TotpVerificationService {

    private final TemporaryAuthSessionRepository temporarySessionRepository;
    private final TotpSecretRepository totpSecretRepository;
    private final TotpSecretEncryptionService encryptionService;
    private final TokenHashService tokenHashService;
    private final TotpService totpService;
    private final AuthenticatedSessionService sessionService;

    private final int maxOtpAttempts;
    private final long otpLockoutMinutes;
    private final long accessTokenExpirationSeconds;
    private final long clockSkewSteps;

    public TotpVerificationService(
            TemporaryAuthSessionRepository temporarySessionRepository,
            TotpSecretRepository totpSecretRepository,
            TotpSecretEncryptionService encryptionService,
            TokenHashService tokenHashService,
            TotpService totpService,
            AuthenticatedSessionService sessionService,
            @Value("${app.security.max-otp-attempts:5}")
            int maxOtpAttempts,
            @Value("${app.security.otp-lockout-minutes:15}")
            long otpLockoutMinutes,
            @Value("${app.security.access-token-expiration-seconds:900}")
            long accessTokenExpirationSeconds,
            @Value("${app.security.totp-clock-skew-steps:1}")
            long clockSkewSteps
    ) {
        this.temporarySessionRepository =
                temporarySessionRepository;

        this.totpSecretRepository =
                totpSecretRepository;

        this.encryptionService =
                encryptionService;

        this.tokenHashService =
                tokenHashService;

        this.totpService =
                totpService;

        this.sessionService =
                sessionService;

        this.maxOtpAttempts =
                maxOtpAttempts;

        this.otpLockoutMinutes =
                otpLockoutMinutes;

        this.accessTokenExpirationSeconds =
                accessTokenExpirationSeconds;

        this.clockSkewSteps =
                clockSkewSteps;
    }

    @Transactional
    public TotpVerificationResult verify(
            String temporaryAuthSessionToken,
            String submittedOtp,
            String deviceId,
            String deviceName,
            String ipAddress
    ) {

        byte[] sessionHash =
                tokenHashService.hash(
                        temporaryAuthSessionToken
                );

        TemporaryAuthSession temporarySession =
                temporarySessionRepository
                        .findBySessionTokenHash(sessionHash)
                        .orElse(null);

        if (temporarySession == null) {
            return TotpVerificationResult.failure(
                    TotpVerificationResult.Status.INVALID_OTP
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (temporarySession.getStatus()
                != TemporaryAuthSession.Status.PENDING) {

            if (temporarySession.getStatus()
                    == TemporaryAuthSession.Status.LOCKED) {

                return TotpVerificationResult.failure(
                        TotpVerificationResult.Status.LOCKED
                );
            }

            return TotpVerificationResult.failure(
                    TotpVerificationResult.Status.INVALID_OTP
            );
        }

        if (!temporarySession.getExpiresAt()
                .isAfter(now)) {

            temporarySession.setStatus(
                    TemporaryAuthSession.Status.EXPIRED
            );

            temporarySessionRepository.save(
                    temporarySession
            );

            return TotpVerificationResult.failure(
                    TotpVerificationResult.Status.EXPIRED_OTP
            );
        }

        User user =
                temporarySession.getUser();

        Optional<TotpSecret> secretOptional =
                totpSecretRepository.findByUser(user);

        if (secretOptional.isEmpty()) {

            throw new IllegalStateException(
                    "TOTP credential is unavailable"
            );
        }

        TotpSecret totpSecret =
                secretOptional.get();

        String secret =
                encryptionService.decrypt(
                        totpSecret.getEncryptedSecret()
                );

        long currentUnixTime =
                System.currentTimeMillis() / 1000L;

        long periodSeconds =
                totpSecret.getPeriodSeconds();

        int digits =
                totpSecret.getDigits();

        long currentStep =
                currentUnixTime / periodSeconds;

        Long matchedStep = null;

        for (
                long offset = -clockSkewSteps;
                offset <= clockSkewSteps;
                offset++
        ) {

            long candidateStep =
                    currentStep + offset;

            long candidateTime =
                    candidateStep * periodSeconds;

            String expectedOtp =
                    totpService.generateCode(
                            secret,
                            candidateTime,
                            periodSeconds,
                            digits
                    );

            if (constantTimeEquals(
                    expectedOtp,
                    submittedOtp
            )) {
                matchedStep = candidateStep;
                break;
            }
        }

        if (matchedStep == null) {

            int attempts =
                    temporarySession.getFailedAttempts() + 1;

            temporarySession.setFailedAttempts(
                    attempts
            );

            if (attempts >= maxOtpAttempts) {

                temporarySession.setStatus(
                        TemporaryAuthSession.Status.LOCKED
                );
            }

            temporarySessionRepository.save(
                    temporarySession
            );

            if (attempts >= maxOtpAttempts) {
                return TotpVerificationResult.failure(
                        TotpVerificationResult.Status.LOCKED
                );
            }

            return TotpVerificationResult.failure(
                    TotpVerificationResult.Status.INVALID_OTP
            );
        }

        int consumed =
                temporarySessionRepository.tryConsumeTotpStep(
                        user.getId(),
                        matchedStep,
                        now
                );

        if (consumed == 0) {

            return TotpVerificationResult.failure(
                    TotpVerificationResult.Status.REPLAY
            );
        }

        temporarySession.setFailedAttempts(0);

        temporarySession.setStatus(
                TemporaryAuthSession.Status.VERIFIED
        );

        temporarySessionRepository.save(
                temporarySession
        );

        AuthenticatedSessionService.TokenPair tokenPair =
                sessionService.createSession(
                        user,
                        deviceId,
                        deviceName,
                        ipAddress
                );

        return TotpVerificationResult.success(
                tokenPair,
                accessTokenExpirationSeconds
        );
    }

    private boolean constantTimeEquals(
            String expected,
            String supplied
    ) {

        if (expected == null
                || supplied == null) {

            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(
                        StandardCharsets.UTF_8
                ),
                supplied.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }
}