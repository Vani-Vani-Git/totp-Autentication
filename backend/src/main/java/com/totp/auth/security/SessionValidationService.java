package com.totp.auth.security;

import com.totp.auth.entity.AuthenticatedSession;
import com.totp.auth.repository.AuthenticatedSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionValidationService {

    private final AuthenticatedSessionRepository sessionRepository;

    public SessionValidationService(
            AuthenticatedSessionRepository sessionRepository
    ) {
        this.sessionRepository = sessionRepository;
    }

    public AuthenticatedSession validateSession(
            UUID accessTokenJti
    ) {
        if (accessTokenJti == null) {
            return null;
        }

        AuthenticatedSession session =
                sessionRepository
                        .findByAccessTokenJti(accessTokenJti)
                        .orElse(null);

        if (session == null) {
            return null;
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (session.getRevokedAt() != null) {
            return null;
        }

        if (session.getExpiresAt() == null
                || !session.getExpiresAt().isAfter(now)) {
            return null;
        }

        if (session.getUser() == null) {
            return null;
        }

        return session;
    }
}