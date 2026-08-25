package com.totp.auth.service;

import com.totp.auth.dto.RegisterRequest;
import com.totp.auth.dto.RegisterResponse;
import com.totp.auth.entity.User;
import com.totp.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthRegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(
            RegisterRequest request
    ) {

        String normalizedUserId =
                request.userId().trim();

        String normalizedEmail =
                request.email().trim().toLowerCase();

        if (userRepository.existsByUserId(
                normalizedUserId
        )) {
            throw new IllegalArgumentException(
                    "User ID already exists"
            );
        }

        if (userRepository.existsByEmail(
                normalizedEmail
        )) {
            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        User user = new User();

        user.setUserId(normalizedUserId);

        user.setEmail(normalizedEmail);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setStatus("ACTIVE");

        user.setTotpEnabled(false);

        user.setFailedLoginAttempts(0);

        user.setLockedUntil(null);

        User savedUser =
                userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getStatus(),
                savedUser.isTotpEnabled()
        );
    }
}