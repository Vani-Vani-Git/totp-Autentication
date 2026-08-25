package com.totp.auth.repository;

import com.totp.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdOrEmail(
            String userId,
            String email
    );

    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);
}