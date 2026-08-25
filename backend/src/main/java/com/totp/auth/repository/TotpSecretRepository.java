package com.totp.auth.repository;

import com.totp.auth.entity.TotpSecret;
import com.totp.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TotpSecretRepository
        extends JpaRepository<TotpSecret, Long> {

    Optional<TotpSecret> findByUser(User user);

    Optional<TotpSecret> findByUserId(Long userId);

    boolean existsByUser(User user);

    boolean existsByUserId(Long userId);
}