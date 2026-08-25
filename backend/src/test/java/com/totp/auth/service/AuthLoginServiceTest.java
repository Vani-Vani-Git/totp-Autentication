package com.totp.auth.service;

import com.totp.auth.dto.LoginRequest;
import com.totp.auth.entity.User;
import com.totp.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import java.util.UUID;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.totp.auth.repository.TemporaryAuthSessionRepository;
import com.totp.auth.security.SecureTokenService;
import com.totp.auth.security.TokenHashService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticatedSessionService sessionService;

    @Mock
    private TemporaryAuthSessionRepository temporaryAuthSessionRepository;

    @Mock
    private SecureTokenService secureTokenService;

    @Mock
    private TokenHashService tokenHashService;

    private AuthLoginService loginService;

    private User user;

    @BeforeEach
    void setUp() {

        loginService = new AuthLoginService(
                userRepository,
                passwordEncoder,
                sessionService,
                temporaryAuthSessionRepository,
                secureTokenService,
                tokenHashService,
                120L
        );
        user = new User();

        user.setUserId("testuser01");
        user.setEmail("testuser01@example.com");
        user.setPasswordHash("hashed-password");
        user.setStatus("ACTIVE");
        user.setTotpEnabled(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
    }

    @Test
    void shouldAuthenticateWithValidUserIdAndPassword() {

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "TestPassword@123"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "TestPassword@123",
                        "hashed-password"
                )
        ).thenReturn(true);

        User result =
                loginService.authenticateCredentials(
                        request
                );

        assertNotNull(result);
        assertEquals(
                "testuser01",
                result.getUserId()
        );

        assertEquals(
                0,
                result.getFailedLoginAttempts()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldAuthenticateWithEmail() {

        LoginRequest request =
                new LoginRequest(
                        "testuser01@example.com",
                        "TestPassword@123"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01@example.com",
                        "testuser01@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "TestPassword@123",
                        "hashed-password"
                )
        ).thenReturn(true);

        User result =
                loginService.authenticateCredentials(
                        request
                );

        assertNotNull(result);

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectInvalidPassword() {

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "WrongPassword"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "WrongPassword",
                        "hashed-password"
                )
        ).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> loginService
                                .authenticateCredentials(request)
                );

        assertEquals(
                "Invalid credentials",
                exception.getMessage()
        );

        assertEquals(
                1,
                user.getFailedLoginAttempts()
        );

        assertNull(
                user.getLockedUntil()
        );

        verify(userRepository)
                .save(user);
    }

    @Test
    void shouldLockAccountAfterFiveFailedAttempts() {

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "WrongPassword"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "WrongPassword",
                        "hashed-password"
                )
        ).thenReturn(false);

        for (int i = 1; i <= 5; i++) {

            assertThrows(
                    IllegalArgumentException.class,
                    () -> loginService
                            .authenticateCredentials(request)
            );
        }

        assertEquals(
                5,
                user.getFailedLoginAttempts()
        );

        assertNotNull(
                user.getLockedUntil()
        );

        assertTrue(
                user.getLockedUntil()
                        .isAfter(LocalDateTime.now())
        );

        verify(
                userRepository,
                times(5)
        ).save(user);
    }

    @Test
    void shouldRejectAlreadyLockedAccount() {

        user.setFailedLoginAttempts(5);

        user.setLockedUntil(
                LocalDateTime.now()
                        .plusMinutes(10)
        );

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "TestPassword@123"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> loginService
                                .authenticateCredentials(request)
                );

        assertEquals(
                "Account is temporarily locked",
                exception.getMessage()
        );

        verify(
                passwordEncoder,
                never()
        ).matches(
                any(),
                any()
        );
    }

    @Test
    void shouldRejectInactiveAccount() {

        user.setStatus("DISABLED");

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "TestPassword@123"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> loginService
                                .authenticateCredentials(request)
                );

        assertEquals(
                "Account is not active",
                exception.getMessage()
        );

        verify(
                passwordEncoder,
                never()
        ).matches(
                any(),
                any()
        );
    }

    @Test
    void shouldResetFailedAttemptsAfterSuccessfulLogin() {

        user.setFailedLoginAttempts(3);

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "TestPassword@123"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "TestPassword@123",
                        "hashed-password"
                )
        ).thenReturn(true);

        User result =
                loginService.authenticateCredentials(
                        request
                );

        assertNotNull(result);

        assertEquals(
                0,
                user.getFailedLoginAttempts()
        );

        assertNull(
                user.getLockedUntil()
        );

        verify(userRepository)
                .save(user);
    }

    @Test
    void shouldUseGenericErrorWhenUserDoesNotExist() {

        LoginRequest request =
                new LoginRequest(
                        "unknown-user",
                        "TestPassword@123"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "unknown-user",
                        "unknown-user"
                )
        ).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> loginService
                                .authenticateCredentials(request)
                );

        assertEquals(
                "Invalid credentials",
                exception.getMessage()
        );

        verify(
                passwordEncoder,
                never()
        ).matches(
                any(),
                any()
        );
    }
    @Test
    void shouldCreateSessionWhenTotpIsDisabled() {

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "TestPassword@123"
                );

        user.setTotpEnabled(false);

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "TestPassword@123",
                        "hashed-password"
                )
        ).thenReturn(true);

        UUID sessionId =
                UUID.randomUUID();

        AuthenticatedSessionService.TokenPair tokenPair =
                new AuthenticatedSessionService.TokenPair(
                        "access-token",
                        "refresh-token",
                        sessionId
                );

        when(
                sessionService.createSession(
                        user,
                        "device-001",
                        "Test Device",
                        "127.0.0.1"
                )
        ).thenReturn(tokenPair);

        AuthLoginService.LoginResult result =
                loginService.login(
                        request,
                        "device-001",
                        "Test Device",
                        "127.0.0.1"
                );

        assertNotNull(result);

        assertFalse(
                result.totpRequired()
        );

        assertNotNull(
                result.tokenPair()
        );

        assertEquals(
                "access-token",
                result.tokenPair().accessToken()
        );

        assertEquals(
                "refresh-token",
                result.tokenPair().refreshToken()
        );

        assertEquals(
                sessionId,
                result.tokenPair().sessionId()
        );

        verify(sessionService)
                .createSession(
                        user,
                        "device-001",
                        "Test Device",
                        "127.0.0.1"
                );
    }
    @Test
    void shouldCreateSessionWithoutDeviceMetadata() {

        LoginRequest request =
                new LoginRequest(
                        "testuser01",
                        "TestPassword@123"
                );

        user.setTotpEnabled(false);

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "TestPassword@123",
                        "hashed-password"
                )
        ).thenReturn(true);

        AuthenticatedSessionService.TokenPair tokenPair =
                new AuthenticatedSessionService.TokenPair(
                        "access-token",
                        "refresh-token",
                        UUID.randomUUID()
                );

        when(
                sessionService.createSession(
                        user,
                        null,
                        null,
                        "127.0.0.1"
                )
        ).thenReturn(tokenPair);

        AuthLoginService.LoginResult result =
                loginService.login(
                        request,
                        null,
                        null,
                        "127.0.0.1"
                );

        assertNotNull(result);

        assertFalse(
                result.totpRequired()
        );

        assertNotNull(
                result.tokenPair()
        );

        verify(sessionService)
                .createSession(
                        user,
                        null,
                        null,
                        "127.0.0.1"
                );
    }
}