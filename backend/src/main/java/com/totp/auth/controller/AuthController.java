package com.totp.auth.controller;

import com.totp.auth.dto.LoginRequest;
import com.totp.auth.dto.LoginResponse;
import com.totp.auth.dto.RegisterRequest;
import com.totp.auth.dto.RegisterResponse;
import com.totp.auth.dto.TotpVerificationRequest;
import com.totp.auth.service.AuthLoginService;
import com.totp.auth.service.AuthRegistrationService;
import com.totp.auth.service.TotpVerificationResult;
import com.totp.auth.service.TotpVerificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthRegistrationService registrationService;
    private final AuthLoginService loginService;
    private final TotpVerificationService verificationService;

    public AuthController(
            AuthRegistrationService registrationService,
            AuthLoginService loginService,
            TotpVerificationService verificationService
    ) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.verificationService = verificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response =
                registrationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
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

        AuthLoginService.LoginResult result =
                loginService.login(
                        request,
                        deviceId,
                        deviceName,
                        httpRequest.getRemoteAddr()
                );

        if (result.totpRequired()) {

            LoginResponse response =
                    new LoginResponse(
                            "OTP_REQUIRED",
                            "TOTP verification is required",
                            true,
                            null,
                            null,
                            result.temporaryAuthSessionToken(),
                            result.temporaryAuthSessionExpiresInSeconds(),
                            null
                    );

            return ResponseEntity.ok(response);
        }

        LoginResponse response =
                new LoginResponse(
                        "AUTHENTICATED",
                        "Login successful",
                        false,
                        result.tokenPair().accessToken(),
                        result.tokenPair().refreshToken(),
                        null,
                        null,
                        900L
                );

        return ResponseEntity.ok(response);
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

        return switch (result.status()) {

            case SUCCESS -> ResponseEntity.ok(
                    new OtpSuccessResponse(
                            "SUCCESS",
                            result.tokenPair().accessToken(),
                            result.tokenPair().refreshToken(),
                            result.expiresIn()
                    )
            );

            case LOCKED -> ResponseEntity
                    .status(423)
                    .body(
                            new OtpErrorResponse(
                                    "LOCKED",
                                    "OTP_LOCKED",
                                    "Too many incorrect attempts. Your account has been temporarily locked for security."
                            )
                    );

            case EXPIRED_OTP -> ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new OtpErrorResponse(
                                    "EXPIRED_OTP",
                                    "EXPIRED_OTP",
                                    "This authentication session has expired. Please log in again."
                            )
                    );

            case REPLAY -> ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new OtpErrorResponse(
                                    "INVALID_OTP",
                                    "OTP_REPLAY",
                                    "This verification code has already been used."
                            )
                    );

            case INVALID_OTP -> ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new OtpErrorResponse(
                                    "INVALID_OTP",
                                    "INVALID_OTP",
                                    "Incorrect authentication code."
                            )
                    );
        };
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