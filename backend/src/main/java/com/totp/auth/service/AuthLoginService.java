package com.totp.auth.service;

import com.totp.auth.dto.LoginRequest;
import com.totp.auth.entity.User;
import com.totp.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.totp.auth.entity.TemporaryAuthSession;
import com.totp.auth.repository.TemporaryAuthSessionRepository;
import com.totp.auth.security.SecureTokenService;
import com.totp.auth.security.TokenHashService;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
@Service
public class AuthLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedSessionService sessionService;
    private final TemporaryAuthSessionRepository temporaryAuthSessionRepository;
    private final SecureTokenService secureTokenService;
    private final TokenHashService tokenHashService;
    private final long temporaryAuthSessionTtlSeconds;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    public AuthLoginService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticatedSessionService sessionService,
            TemporaryAuthSessionRepository temporaryAuthSessionRepository,
            SecureTokenService secureTokenService,
            TokenHashService tokenHashService,
            @Value("${app.security.temp-auth-session-ttl-seconds:120}")
            long temporaryAuthSessionTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.temporaryAuthSessionRepository =
                temporaryAuthSessionRepository;
        this.secureTokenService = secureTokenService;
        this.tokenHashService = tokenHashService;
        this.temporaryAuthSessionTtlSeconds =
                temporaryAuthSessionTtlSeconds;
    }

    public User authenticateCredentials(
            LoginRequest request
    ) {

        String identifier =
                request.identifier().trim();

        User user =
                userRepository
                        .findByUserIdOrEmail(
                                identifier,
                                identifier.toLowerCase()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid credentials"
                                )
                        );

        validateAccountStatus(user);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {

            registerFailedLogin(user);

            throw new IllegalArgumentException(
                    "Invalid credentials"
            );
        }

        resetFailedLoginAttempts(user);

        return user;
    }

    public LoginResult login(
            LoginRequest request,
            String deviceId,
            String deviceName,
            String ipAddress
    ) {

        User user =
                authenticateCredentials(request);

        if (user.isTotpEnabled()) {

            String temporaryToken =
                    secureTokenService.generateToken(32);

            byte[] temporaryTokenHash =
                    tokenHashService.hash(temporaryToken);

            TemporaryAuthSession temporarySession =
                    new TemporaryAuthSession();

            temporarySession.setSessionTokenHash(
                    temporaryTokenHash
            );

            temporarySession.setUser(user);

            temporarySession.setExpiresAt(
                    LocalDateTime.now()
                            .plusSeconds(
                                    temporaryAuthSessionTtlSeconds
                            )
            );

            temporarySession.setStatus(
                    TemporaryAuthSession.Status.PENDING
            );

            temporaryAuthSessionRepository.save(
                    temporarySession
            );

            return new LoginResult(
                    user,
                    true,
                    null,
                    temporaryToken,
                    temporaryAuthSessionTtlSeconds
            );
        }

        AuthenticatedSessionService.TokenPair tokenPair =
                sessionService.createSession(
                        user,
                        deviceId,
                        deviceName,
                        ipAddress
                );

        return new LoginResult(
                user,
                false,
                tokenPair,
                null,
                0
        );
    }
    public record LoginResult(
            User user,
            boolean totpRequired,
            AuthenticatedSessionService.TokenPair tokenPair,
            String temporaryAuthSessionToken,
            long temporaryAuthSessionExpiresInSeconds
    ) {
    }
    private void validateAccountStatus(User user) {

        if (!"ACTIVE".equalsIgnoreCase(
                user.getStatus()
        )) {
            throw new IllegalArgumentException(
                    "Account is not active"
            );
        }

        LocalDateTime lockedUntil =
                user.getLockedUntil();

        if (lockedUntil != null
                && lockedUntil.isAfter(
                LocalDateTime.now()
        )) {

            throw new IllegalArgumentException(
                    "Account is temporarily locked"
            );
        }
    }
    private void registerFailedLogin(User user) {

        int attempts =
                user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {

            user.setLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(
                                    LOCK_DURATION_MINUTES
                            )
            );
        }

        userRepository.save(user);
    }
    private void resetFailedLoginAttempts(User user) {

        if (user.getFailedLoginAttempts() != 0
                || user.getLockedUntil() != null) {

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            userRepository.save(user);
        }
    }
}