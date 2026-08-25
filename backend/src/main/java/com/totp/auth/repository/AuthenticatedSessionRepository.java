package com.totp.auth.repository;

import com.totp.auth.entity.AuthenticatedSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthenticatedSessionRepository
        extends JpaRepository<AuthenticatedSession, Long> {

    Optional<AuthenticatedSession> findBySessionId(UUID sessionId);

    Optional<AuthenticatedSession> findByRefreshTokenHash(
            byte[] refreshTokenHash
    );

    Optional<AuthenticatedSession> findByAccessTokenJti(
            UUID accessTokenJti
    );

    List<AuthenticatedSession> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<AuthenticatedSession> findByUserIdAndRevokedAtIsNullOrderByCreatedAtDesc(
            Long userId
    );
}