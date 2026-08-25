package com.totp.auth.repository;

import com.totp.auth.entity.PasswordRecoveryCode;
import com.totp.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordRecoveryCodeRepository
        extends JpaRepository<PasswordRecoveryCode, Long> {

    Optional<PasswordRecoveryCode> findTopByUserAndStatusOrderByCreatedAtDesc(
            User user,
            String status
    );
}