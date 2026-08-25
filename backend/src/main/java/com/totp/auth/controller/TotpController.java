package com.totp.auth.controller;

import com.totp.auth.dto.TotpConfirmRequest;
import com.totp.auth.dto.TotpEnrollmentResponse;
import com.totp.auth.entity.User;
import com.totp.auth.repository.UserRepository;
import com.totp.auth.service.TotpEnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.totp.auth.service.TotpConfirmationService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import com.totp.auth.dto.TotpVerificationRequest;
import com.totp.auth.service.TotpVerificationResult;
import com.totp.auth.service.TotpVerificationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/totp")
public class TotpController {

    private final TotpEnrollmentService enrollmentService;
    private final TotpConfirmationService confirmationService;
    private final UserRepository userRepository;
    private final TotpVerificationService verificationService;

    public TotpController(
            TotpEnrollmentService enrollmentService,
            TotpConfirmationService confirmationService,
            UserRepository userRepository,
            TotpVerificationService verificationService
    ) {
        this.enrollmentService =
                enrollmentService;

        this.confirmationService =
                confirmationService;

        this.userRepository =
                userRepository;

        this.verificationService =
                verificationService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<TotpEnrollmentResponse> enroll(
            Authentication authentication
    ) {

        String userIdentifier =
                authentication.getName();

        User user =
                userRepository
                        .findByUserIdOrEmail(
                                userIdentifier,
                                userIdentifier
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Authenticated user was not found"
                                )
                        );

        TotpEnrollmentResponse response =
                enrollmentService.enroll(user);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(
            @Valid @RequestBody TotpConfirmRequest request,
            Authentication authentication
    ) {

        String userIdentifier =
                authentication.getName();

        User user =
                userRepository
                        .findByUserIdOrEmail(
                                userIdentifier,
                                userIdentifier
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Authenticated user was not found"
                                )
                        );

        confirmationService.confirm(
                user,
                request.code()
        );

        return ResponseEntity.ok().build();
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody TotpVerificationRequest request,
            @RequestHeader(
                    value = "X-Device-ID",
                    required = false
            )
            String deviceId,
            @RequestHeader(
                    value = "X-Device-Name",
                    required = false
            )
            String deviceName,
            HttpServletRequest httpRequest
    ) {

        TotpVerificationResult result =
                verificationService.verify(
                        request.tempAuthSessionId(),
                        request.otp(),
                        deviceId,
                        deviceName,
                        httpRequest.getRemoteAddr()
                );

        if (result.status()
                == TotpVerificationResult.Status.SUCCESS) {

            return ResponseEntity.ok(
                    new OtpSuccessResponse(
                            "SUCCESS",
                            result.tokenPair().accessToken(),
                            result.tokenPair().refreshToken(),
                            result.expiresIn()
                    )
            );
        }

        if (result.status()
                == TotpVerificationResult.Status.LOCKED) {

            return ResponseEntity
                    .status(423)
                    .body(
                            new OtpErrorResponse(
                                    "LOCKED",
                                    "OTP_LOCKED",
                                    "Too many incorrect attempts. Your account has been temporarily locked for security."
                            )
                    );
        }

        if (result.status()
                == TotpVerificationResult.Status.EXPIRED_OTP) {

            return ResponseEntity
                    .status(401)
                    .body(
                            new OtpErrorResponse(
                                    "EXPIRED_OTP",
                                    "EXPIRED_OTP",
                                    "This code has expired. Enter the latest code shown in your authenticator app."
                            )
                    );
        }

        if (result.status()
                == TotpVerificationResult.Status.REPLAY) {

            return ResponseEntity
                    .status(401)
                    .body(
                            new OtpErrorResponse(
                                    "INVALID_OTP",
                                    "OTP_REPLAY",
                                    "This verification code has already been used."
                            )
                    );
        }

        return ResponseEntity
                .status(401)
                .body(
                        new OtpErrorResponse(
                                "INVALID_OTP",
                                "INVALID_OTP",
                                "Incorrect code. Please check the current code in your authenticator app and try again."
                        )
                );
    }

    public record OtpSuccessResponse(
            String status,
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {
    }

    public record OtpErrorResponse(
            String status,
            String errorCode,
            String errorMessage
    ) {
    }
}