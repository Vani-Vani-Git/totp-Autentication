package com.totp.auth.repository;

import com.totp.auth.entity.TemporaryAuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TemporaryAuthSessionRepository
        extends JpaRepository<TemporaryAuthSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TemporaryAuthSession> findBySessionTokenHash(
            byte[] sessionTokenHash
    );

    @Modifying
    @Query(
            value = """
                    INSERT IGNORE INTO totp_consumed_steps
                    (user_id, time_step, used_at)
                    VALUES (:userId, :timeStep, :usedAt)
                    """,
            nativeQuery = true
    )
    int tryConsumeTotpStep(
            @Param("userId") Long userId,
            @Param("timeStep") Long timeStep,
            @Param("usedAt") LocalDateTime usedAt
    );

    long deleteByExpiresAtBefore(LocalDateTime time);
}