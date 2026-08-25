package com.totp.auth.repository;

import com.totp.auth.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AuditLog> findByEventTypeOrderByCreatedAtDesc(
            String eventType
    );

    List<AuditLog> findByCorrelationId(String correlationId);
}