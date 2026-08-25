package com.totp.auth.controller;

import com.totp.auth.dto.TotpEnrollmentResponse;
import com.totp.auth.entity.User;
import com.totp.auth.repository.UserRepository;
import com.totp.auth.service.TotpEnrollmentService;
import com.totp.auth.service.TotpVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.totp.auth.dto.TotpConfirmRequest;
import com.totp.auth.service.TotpConfirmationService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TotpControllerTest {

    @Mock
    private TotpEnrollmentService enrollmentService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TotpConfirmationService confirmationService;

    @Mock
    private TotpVerificationService verificationService;

    private TotpController controller;

    private User user;

    @BeforeEach
    void setUp() {

        controller =
                new TotpController(
                        enrollmentService,
                        confirmationService,
                        userRepository,
                        verificationService
                );

        user = new User();

        user.setUserId("testuser01");
        user.setEmail("testuser01@example.com");
        user.setStatus("ACTIVE");
        user.setTotpEnabled(false);
    }

    @Test
    void shouldEnrollForAuthenticatedUser() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "testuser01",
                        null
                );

        TotpEnrollmentResponse response =
                new TotpEnrollmentResponse(
                        "JBSWY3DPEHPK3PXP",
                        "otpauth://totp/TOTP%20Authentication:testuser01%40example.com?secret=JBSWY3DPEHPK3PXP&issuer=TOTP%20Authentication&algorithm=SHA1&digits=6&period=30",
                        "TOTP Authentication",
                        "testuser01@example.com",
                        6,
                        30,
                        "SHA1"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        when(
                enrollmentService.enroll(user)
        ).thenReturn(response);

        ResponseEntity<TotpEnrollmentResponse> result =
                controller.enroll(authentication);

        assertEquals(
                200,
                result.getStatusCode().value()
        );

        assertNotNull(
                result.getBody()
        );

        assertEquals(
                "JBSWY3DPEHPK3PXP",
                result.getBody().secret()
        );

        verify(
                enrollmentService
        ).enroll(user);
    }

    @Test
    void shouldLookupUserUsingAuthenticatedIdentity() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "testuser01@example.com",
                        null
                );

        TotpEnrollmentResponse response =
                new TotpEnrollmentResponse(
                        "JBSWY3DPEHPK3PXP",
                        "otpauth://totp/test",
                        "TOTP Authentication",
                        "testuser01@example.com",
                        6,
                        30,
                        "SHA1"
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01@example.com",
                        "testuser01@example.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                enrollmentService.enroll(user)
        ).thenReturn(response);

        controller.enroll(authentication);

        verify(
                userRepository
        ).findByUserIdOrEmail(
                "testuser01@example.com",
                "testuser01@example.com"
        );

        verify(
                enrollmentService
        ).enroll(user);
    }

    @Test
    void shouldRejectWhenAuthenticatedUserCannotBeFound() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "unknown-user",
                        null
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "unknown-user",
                        "unknown-user"
                )
        ).thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> controller.enroll(authentication)
                );

        assertEquals(
                "Authenticated user was not found",
                exception.getMessage()
        );

        verify(
                enrollmentService,
                never()
        ).enroll(any(User.class));
    }

    @Test
    void shouldNeverUseClientSuppliedUserId() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "testuser01",
                        null
                );

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        TotpEnrollmentResponse response =
                new TotpEnrollmentResponse(
                        "JBSWY3DPEHPK3PXP",
                        "otpauth://totp/test",
                        "TOTP Authentication",
                        "testuser01@example.com",
                        6,
                        30,
                        "SHA1"
                );

        when(
                enrollmentService.enroll(user)
        ).thenReturn(response);

        controller.enroll(authentication);

        verify(
                enrollmentService
        ).enroll(user);

        verifyNoMoreInteractions(
                enrollmentService
        );
    }
    @Test
    void shouldConfirmTotpForAuthenticatedUser() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "testuser01",
                        null
                );

        TotpConfirmRequest request =
                new TotpConfirmRequest("123456");

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01",
                        "testuser01"
                )
        ).thenReturn(Optional.of(user));

        doNothing()
                .when(confirmationService)
                .confirm(
                        user,
                        "123456"
                );

        ResponseEntity<Void> result =
                controller.confirm(
                        request,
                        authentication
                );

        assertEquals(
                200,
                result.getStatusCode().value()
        );

        verify(
                confirmationService
        ).confirm(
                user,
                "123456"
        );
    }
    @Test
    void shouldUseAuthenticatedIdentityForTotpConfirmation() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "testuser01@example.com",
                        null
                );

        TotpConfirmRequest request =
                new TotpConfirmRequest("654321");

        when(
                userRepository.findByUserIdOrEmail(
                        "testuser01@example.com",
                        "testuser01@example.com"
                )
        ).thenReturn(Optional.of(user));

        controller.confirm(
                request,
                authentication
        );

        verify(
                confirmationService
        ).confirm(
                user,
                "654321"
        );
    }
}