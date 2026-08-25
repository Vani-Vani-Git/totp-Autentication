package com.totp.auth.repository;

import com.totp.auth.entity.TotpConsumedStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TotpConsumedStepRepository
        extends JpaRepository<TotpConsumedStep, Long> {

    boolean existsByUserIdAndTimeStep(
            Long userId,
            Long timeStep
    );
}