package com.totp.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private final Long userId = 100L;
    private final String userIdentifier = "user@example.com";
    private final UUID sessionId =
            UUID.randomUUID();
    private final UUID accessTokenJti =
            UUID.randomUUID();

    @BeforeEach
    void setUp() {

        String secret =
                "VGhpc0lzQVNlY3VyZUpXVFNlY3JldEtleUZvckRldk9ubHk=";

        jwtService = new JwtService(
                secret,
                900L
        );
    }

    @Test
    void shouldGenerateValidAccessToken() {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldValidateGeneratedToken() {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        Jws<Claims> parsed =
                jwtService.parseAndValidate(token);

        assertNotNull(parsed);

        Claims claims =
                parsed.getPayload();

        assertEquals(
                userIdentifier,
                claims.getSubject()
        );

        assertEquals(
                userId,
                claims.get("userId", Long.class)
        );

        assertEquals(
                accessTokenJti.toString(),
                claims.getId()
        );
    }

    @Test
    void shouldExtractJti() {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        UUID extractedJti =
                jwtService.extractAccessTokenJti(token);

        assertEquals(
                accessTokenJti,
                extractedJti
        );
    }

    @Test
    void shouldExtractSessionId() {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        UUID extractedSessionId =
                jwtService.extractSessionId(token);

        assertEquals(
                sessionId,
                extractedSessionId
        );
    }

    @Test
    void shouldExtractSubject() {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        String subject =
                jwtService.extractSubject(token);

        assertEquals(
                userIdentifier,
                subject
        );
    }

    @Test
    void shouldRejectTamperedToken() {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        String tamperedToken =
                token.substring(0, token.length() - 2)
                        + "xx";

        assertThrows(
                Exception.class,
                () -> jwtService.parseAndValidate(
                        tamperedToken
                )
        );
    }
}
