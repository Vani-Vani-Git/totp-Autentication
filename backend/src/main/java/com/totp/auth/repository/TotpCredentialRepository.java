package com.totp.auth.repository;

import com.totp.auth.entity.TotpCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TotpCredentialRepository
        extends JpaRepository<TotpCredential, Long> {

    Optional<TotpCredential> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}