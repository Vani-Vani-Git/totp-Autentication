package com.totp.auth.service;

import com.totp.auth.entity.AuthenticatedSession;
import com.totp.auth.entity.User;
import com.totp.auth.repository.AuthenticatedSessionRepository;
import com.totp.auth.security.JwtService;
import com.totp.auth.security.SecureTokenService;
import com.totp.auth.security.TokenHashService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticatedSessionService {

    private final AuthenticatedSessionRepository sessionRepository;
    private final SecureTokenService secureTokenService;
    private final TokenHashService tokenHashService;
    private final JwtService jwtService;

    private final long refreshTokenExpirationSeconds;

    public AuthenticatedSessionService(
            AuthenticatedSessionRepository sessionRepository,
            SecureTokenService secureTokenService,
            TokenHashService tokenHashService,
            JwtService jwtService,
            @Value("${app.security.refresh-token-expiration-seconds}")
            long refreshTokenExpirationSeconds
    ) {
        this.sessionRepository = sessionRepository;
        this.secureTokenService = secureTokenService;
        this.tokenHashService = tokenHashService;
        this.jwtService = jwtService;
        this.refreshTokenExpirationSeconds =
                refreshTokenExpirationSeconds;
    }

    @Transactional
    public TokenPair createSession(
            User user,
            String deviceId,
            String deviceName,
            String ipAddress
    ) {

        UUID sessionId =
                secureTokenService.generateUuid();

        UUID accessTokenJti =
                secureTokenService.generateUuid();

        String refreshToken =
                secureTokenService.generateToken(64);

        byte[] refreshTokenHash =
                tokenHashService.hash(refreshToken);

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusSeconds(
                                refreshTokenExpirationSeconds
                        );

        AuthenticatedSession session =
                new AuthenticatedSession();

        session.setSessionId(sessionId);
        session.setUser(user);
        session.setRefreshTokenHash(
                refreshTokenHash
        );
        session.setDeviceId(deviceId);
        session.setDeviceName(deviceName);
        session.setIpAddress(ipAddress);
        session.setAccessTokenJti(
                accessTokenJti
        );
        session.setExpiresAt(expiresAt);

        sessionRepository.save(session);

        String accessToken =
                jwtService.generateAccessToken(
                        user.getId(),
                        user.getUserId(),
                        sessionId,
                        accessTokenJti
                );

        return new TokenPair(
                accessToken,
                refreshToken,
                sessionId
        );
    }

    public record TokenPair(
            String accessToken,
            String refreshToken,
            UUID sessionId
    ) {
    }
}