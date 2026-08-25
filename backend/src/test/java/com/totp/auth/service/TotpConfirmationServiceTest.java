package com.totp.auth.service;

import com.totp.auth.entity.TotpSecret;
import com.totp.auth.entity.User;
import com.totp.auth.repository.TotpSecretRepository;
import com.totp.auth.repository.UserRepository;
import com.totp.auth.security.TotpSecretEncryptionService;
import com.totp.auth.security.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TotpConfirmationServiceTest {

    @Mock
    private TotpSecretRepository totpSecretRepository;

    @Mock
    private TotpSecretEncryptionService encryptionService;

    @Mock
    private TotpService totpService;

    @Mock
    private UserRepository userRepository;

    private TotpConfirmationService confirmationService;

    private User user;

    private TotpSecret totpSecret;

    @BeforeEach
    void setUp() {

        confirmationService =
                new TotpConfirmationService(
                        totpSecretRepository,
                        encryptionService,
                        totpService,
                        userRepository
                );

        user = new User();

        user.setUserId("testuser01");
        user.setEmail("testuser01@example.com");
        user.setStatus("ACTIVE");
        user.setTotpEnabled(false);

        totpSecret = new TotpSecret();

        totpSecret.setUser(user);
        totpSecret.setEncryptedSecret(
                "encrypted-secret"
        );
        totpSecret.setAlgorithm("SHA1");
        totpSecret.setDigits(6);
        totpSecret.setPeriodSeconds(30);
    }

    @Test
    void shouldEnableTotpWhenCodeIsValid() {

        when(
                totpSecretRepository.findByUser(user)
        ).thenReturn(
                Optional.of(totpSecret)
        );

        when(
                encryptionService.decrypt(
                        "encrypted-secret"
                )
        ).thenReturn(
                "JBSWY3DPEHPK3PXP"
        );

        when(
                totpService.verifyCode(
                        eq("JBSWY3DPEHPK3PXP"),
                        eq("123456"),
                        anyLong()
                )
        ).thenReturn(true);

        confirmationService.confirm(
                user,
                "123456"
        );

        assertTrue(
                user.isTotpEnabled()
        );

        verify(
                encryptionService
        ).decrypt(
                "encrypted-secret"
        );

        verify(
                totpService
        ).verifyCode(
                eq("JBSWY3DPEHPK3PXP"),
                eq("123456"),
                anyLong()
        );
    }

    @Test
    void shouldNotEnableTotpWhenCodeIsInvalid() {

        when(
                totpSecretRepository.findByUser(user)
        ).thenReturn(
                Optional.of(totpSecret)
        );

        when(
                encryptionService.decrypt(
                        "encrypted-secret"
                )
        ).thenReturn(
                "JBSWY3DPEHPK3PXP"
        );

        when(
                totpService.verifyCode(
                        eq("JBSWY3DPEHPK3PXP"),
                        eq("123456"),
                        anyLong()
                )
        ).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> confirmationService.confirm(
                                user,
                                "123456"
                        )
                );

        assertEquals(
                "Invalid TOTP code",
                exception.getMessage()
        );

        assertFalse(
                user.isTotpEnabled()
        );
    }

    @Test
    void shouldRejectMissingEnrollment() {

        when(
                totpSecretRepository.findByUser(user)
        ).thenReturn(
                Optional.empty()
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> confirmationService.confirm(
                                user,
                                "123456"
                        )
                );

        assertEquals(
                "TOTP enrollment was not found",
                exception.getMessage()
        );

        assertFalse(
                user.isTotpEnabled()
        );

        verifyNoInteractions(
                encryptionService,
                totpService
        );
    }

    @Test
    void shouldRejectAlreadyEnabledTotp() {

        user.setTotpEnabled(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> confirmationService.confirm(
                                user,
                                "123456"
                        )
                );

        assertEquals(
                "TOTP is already enabled",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService,
                totpService
        );
    }

    @Test
    void shouldRejectNullUser() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> confirmationService.confirm(
                                null,
                                "123456"
                        )
                );

        assertEquals(
                "User is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService,
                totpService
        );
    }

    @Test
    void shouldRejectBlankCode() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> confirmationService.confirm(
                                user,
                                "   "
                        )
                );

        assertEquals(
                "TOTP code is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService,
                totpService
        );
    }

    @Test
    void shouldRejectNullCode() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> confirmationService.confirm(
                                user,
                                null
                        )
                );

        assertEquals(
                "TOTP code is required",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService,
                totpService
        );
    }

    @Test
    void shouldRejectCodeWithWrongLength() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> confirmationService.confirm(
                                user,
                                "12345"
                        )
                );

        assertEquals(
                "TOTP code must contain exactly 6 digits",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService,
                totpService
        );
    }

    @Test
    void shouldRejectCodeContainingLetters() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> confirmationService.confirm(
                                user,
                                "12AB56"
                        )
                );

        assertEquals(
                "TOTP code must contain exactly 6 digits",
                exception.getMessage()
        );

        verifyNoInteractions(
                totpSecretRepository,
                encryptionService,
                totpService
        );
    }

    @Test
    void shouldTrimValidCodeBeforeVerification() {

        when(
                totpSecretRepository.findByUser(user)
        ).thenReturn(
                Optional.of(totpSecret)
        );

        when(
                encryptionService.decrypt(
                        "encrypted-secret"
                )
        ).thenReturn(
                "JBSWY3DPEHPK3PXP"
        );

        when(
                totpService.verifyCode(
                        eq("JBSWY3DPEHPK3PXP"),
                        eq("123456"),
                        anyLong()
                )
        ).thenReturn(true);

        confirmationService.confirm(
                user,
                " 123456 "
        );

        assertTrue(
                user.isTotpEnabled()
        );

        verify(
                totpService
        ).verifyCode(
                eq("JBSWY3DPEHPK3PXP"),
                eq("123456"),
                anyLong()
        );
    }
}