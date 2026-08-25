package com.totp.auth.service;

import com.totp.auth.dto.TotpEnrollmentResponse;
import com.totp.auth.entity.TotpSecret;
import com.totp.auth.entity.User;
import com.totp.auth.repository.TotpSecretRepository;
import com.totp.auth.security.Base32Codec;
import com.totp.auth.security.TotpSecretEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TotpEnrollmentServiceTest {

    @Mock
    private TotpSecretRepository totpSecretRepository;

    @Mock
    private TotpSecretEncryptionService encryptionService;

    private TotpEnrollmentService enrollmentService;

    private Base32Codec base32Codec;

    private User user;

    @BeforeEach
    void setUp() {

        base32Codec = new Base32Codec();

        enrollmentService =
                new TotpEnrollmentService(
                        totpSecretRepository,
                        encryptionService,
                        base32Codec
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
    void shouldCreateTotpEnrollment() {

        when(
                totpSecretRepository.existsByUser(user)
        ).thenReturn(false);

        when(
                encryptionService.encrypt(any(String.class))
        ).thenReturn("encrypted-secret");

        TotpEnrollmentResponse response =
                enrollmentService.enroll(user);

        assertNotNull(response);

        assertNotNull(response.secret());

        assertFalse(
                response.secret().isBlank()
        );

        assertEquals(
                "TOTP Authentication",
                response.issuer()
        );

        assertEquals(
                "testuser01@example.com",
                response.accountName()
        );

        assertEquals(
                6,
                response.digits()
        );

        assertEquals(
                30,
                response.periodSeconds()
        );

        assertEquals(
                "SHA1",
                response.algorithm()
        );

        assertTrue(
                response.otpauthUri()
                        .startsWith("otpauth://totp/")
        );

        verify(
                encryptionService
        ).encrypt(response.secret());

        ArgumentCaptor<TotpSecret> captor =
                ArgumentCaptor.forClass(
                        TotpSecret.class
                );

        verify(
                totpSecretRepository
        ).save(captor.capture());

        TotpSecret saved =
                captor.getValue();

        assertEquals(
                user,
                saved.getUser()
        );

        assertEquals(
                "encrypted-secret",
                saved.getEncryptedSecret()
        );

        assertEquals(
                "SHA1",
                saved.getAlgorithm()
        );

        assertEquals(
                6,
                saved.getDigits()
        );

        assertEquals(
                30,
                saved.getPeriodSeconds()
        );

        assertFalse(
                user.isTotpEnabled()
        );
    }

    @Test
    void generatedSecretShouldBeValidBase32() {

        when(
                totpSecretRepository.existsByUser(user)
        ).thenReturn(false);

        when(
                encryptionService.encrypt(any(String.class))
        ).thenReturn("encrypted-secret");

        TotpEnrollmentResponse response =
                enrollmentService.enroll(user);

        byte[] decoded =
                base32Codec.decode(
                        response.secret()
                );

        assertNotNull(decoded);

        assertEquals(
                20,
                decoded.length
        );
    }

    @Test
    void shouldCreateOtpAuthUriWithEnrollmentParameters() {

        when(
                totpSecretRepository.existsByUser(user)
        ).thenReturn(false);

        when(
                encryptionService.encrypt(any(String.class))
        ).thenReturn("encrypted-secret");

        TotpEnrollmentResponse response =
                enrollmentService.enroll(user);

        String uri =
                response.otpauthUri();

        assertTrue(
                uri.contains(
                        "secret=" + response.secret()
                )
        );

        assertTrue(
                uri.contains(
                        "issuer=TOTP%20Authentication"
                )
        );

        assertTrue(
                uri.contains(
                        "algorithm=SHA1"
                )
        );

        assertTrue(
                uri.contains(
                        "digits=6"
                )
        );

        assertTrue(
                uri.contains(
                        "period=30"
                )
        );
    }

    @Test
    void shouldRejectNullUser() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> enrollmentService.enroll(null)
                );

        assertEquals(
                "User is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService
        );
    }

    @Test
    void shouldRejectAlreadyEnabledTotp() {

        user.setTotpEnabled(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> enrollmentService.enroll(user)
                );

        assertEquals(
                "TOTP is already enabled",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService
        );
    }

    @Test
    void shouldRejectExistingEnrollment() {

        when(
                totpSecretRepository.existsByUser(user)
        ).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> enrollmentService.enroll(user)
                );

        assertEquals(
                "TOTP enrollment already exists",
                exception.getMessage()
        );

        verify(
                encryptionService,
                never()
        ).encrypt(any());

        verify(
                totpSecretRepository,
                never()
        ).save(any());
    }
}