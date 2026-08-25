package com.totp.auth.service;

import com.totp.auth.entity.AuthenticatedSession;
import com.totp.auth.entity.User;
import com.totp.auth.repository.AuthenticatedSessionRepository;
import com.totp.auth.security.JwtService;
import com.totp.auth.security.SecureTokenService;
import com.totp.auth.security.TokenHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatedSessionServiceTest {

    @Mock
    private AuthenticatedSessionRepository sessionRepository;

    @Mock
    private SecureTokenService secureTokenService;

    @Mock
    private TokenHashService tokenHashService;

    @Mock
    private JwtService jwtService;

    private AuthenticatedSessionService sessionService;

    private User user;

    private UUID sessionId;
    private UUID accessTokenJti;

    @BeforeEach
    void setUp() {

        sessionService =
                new AuthenticatedSessionService(
                        sessionRepository,
                        secureTokenService,
                        tokenHashService,
                        jwtService,
                        604800
                );

        user = new User();

        user.setUserId("testuser01");
        user.setEmail("testuser01@example.com");
        user.setPasswordHash("hashed-password");
        user.setStatus("ACTIVE");

        user.setTotpEnabled(false);

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        sessionId =
                UUID.randomUUID();

        accessTokenJti =
                UUID.randomUUID();
    }

    @Test
    void shouldCreateAuthenticatedSession() {

        String refreshToken =
                "secure-refresh-token";

        byte[] refreshTokenHash =
                new byte[] {
                        1, 2, 3, 4
                };

        String accessToken =
                "access.jwt.token";

        when(
                secureTokenService.generateUuid()
        ).thenReturn(
                sessionId,
                accessTokenJti
        );

        when(
                secureTokenService.generateToken(64)
        ).thenReturn(refreshToken);

        when(
                tokenHashService.hash(refreshToken)
        ).thenReturn(refreshTokenHash);

        when(
                jwtService.generateAccessToken(
                        1L,
                        "testuser01",
                        sessionId,
                        accessTokenJti
                )
        ).thenReturn(accessToken);

        AuthenticatedSessionService.TokenPair result =
                sessionService.createSession(
                        user,
                        "device-001",
                        "Test Device",
                        "127.0.0.1"
                );

        assertNotNull(result);

        assertEquals(
                accessToken,
                result.accessToken()
        );

        assertEquals(
                refreshToken,
                result.refreshToken()
        );

        assertEquals(
                sessionId,
                result.sessionId()
        );

        ArgumentCaptor<AuthenticatedSession>
                sessionCaptor =
                ArgumentCaptor.forClass(
                        AuthenticatedSession.class
                );

        verify(sessionRepository)
                .save(sessionCaptor.capture());

        AuthenticatedSession savedSession =
                sessionCaptor.getValue();

        assertEquals(
                sessionId,
                savedSession.getSessionId()
        );

        assertEquals(
                user,
                savedSession.getUser()
        );

        assertArrayEquals(
                refreshTokenHash,
                savedSession.getRefreshTokenHash()
        );

        assertEquals(
                "device-001",
                savedSession.getDeviceId()
        );

        assertEquals(
                "Test Device",
                savedSession.getDeviceName()
        );

        assertEquals(
                "127.0.0.1",
                savedSession.getIpAddress()
        );

        assertEquals(
                accessTokenJti,
                savedSession.getAccessTokenJti()
        );

        assertNull(
                savedSession.getRevokedAt()
        );

        assertNotNull(
                savedSession.getExpiresAt()
        );

        assertTrue(
                savedSession.getExpiresAt()
                        .isAfter(LocalDateTime.now())
        );
    }

    @Test
    void shouldHashRefreshTokenBeforeSaving() {

        String refreshToken =
                "another-refresh-token";

        byte[] hash =
                new byte[] {
                        10, 20, 30
                };

        when(
                secureTokenService.generateUuid()
        ).thenReturn(
                sessionId,
                accessTokenJti
        );

        when(
                secureTokenService.generateToken(64)
        ).thenReturn(refreshToken);

        when(
                tokenHashService.hash(refreshToken)
        ).thenReturn(hash);

        when(
                jwtService.generateAccessToken(
                        anyLong(),
                        anyString(),
                        any(UUID.class),
                        any(UUID.class)
                )
        ).thenReturn(
                "access-token"
        );

        sessionService.createSession(
                user,
                null,
                null,
                null
        );

        verify(tokenHashService)
                .hash(refreshToken);

        ArgumentCaptor<AuthenticatedSession>
                captor =
                ArgumentCaptor.forClass(
                        AuthenticatedSession.class
                );

        verify(sessionRepository)
                .save(captor.capture());

        assertArrayEquals(
                hash,
                captor.getValue()
                        .getRefreshTokenHash()
        );
    }

    @Test
    void shouldGenerateUniqueSessionIdentifiers() {

        when(
                secureTokenService.generateUuid()
        ).thenReturn(
                sessionId,
                accessTokenJti
        );

        when(
                secureTokenService.generateToken(64)
        ).thenReturn(
                "refresh-token"
        );

        when(
                tokenHashService.hash(
                        "refresh-token"
                )
        ).thenReturn(
                new byte[] {1, 2, 3}
        );

        when(
                jwtService.generateAccessToken(
                        anyLong(),
                        anyString(),
                        any(UUID.class),
                        any(UUID.class)
                )
        ).thenReturn(
                "access-token"
        );

        AuthenticatedSessionService.TokenPair result =
                sessionService.createSession(
                        user,
                        null,
                        null,
                        null
                );

        assertEquals(
                sessionId,
                result.sessionId()
        );

        verify(
                secureTokenService,
                times(2)
        ).generateUuid();
    }

    @Test
    void shouldGenerateRefreshTokenWithExpectedLength() {

        when(
                secureTokenService.generateUuid()
        ).thenReturn(
                sessionId,
                accessTokenJti
        );

        when(
                secureTokenService.generateToken(64)
        ).thenReturn(
                "refresh-token"
        );

        when(
                tokenHashService.hash(
                        "refresh-token"
                )
        ).thenReturn(
                new byte[] {1, 2, 3}
        );

        when(
                jwtService.generateAccessToken(
                        anyLong(),
                        anyString(),
                        any(UUID.class),
                        any(UUID.class)
                )
        ).thenReturn(
                "access-token"
        );

        sessionService.createSession(
                user,
                null,
                null,
                null
        );

        verify(
                secureTokenService
        ).generateToken(64);
    }
}