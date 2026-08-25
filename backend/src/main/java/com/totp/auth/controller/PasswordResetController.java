package com.totp.auth.controller;

import com.totp.auth.dto.PasswordResetCompleteRequest;
import com.totp.auth.dto.PasswordResetRequest;
import com.totp.auth.dto.PasswordResetResponse;
import com.totp.auth.dto.PasswordResetVerifyRequest;
import com.totp.auth.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService
    ) {
        this.passwordResetService =
                passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<PasswordResetResponse> requestReset(
            @RequestBody PasswordResetRequest request
    ) {
        String resetToken =
                passwordResetService.createResetToken(
                        request.identifier()
                );

        /*
         * Keep the external response generic.
         * Do not reveal whether the identifier
         * belongs to an account.
         */
        return ResponseEntity.ok(
                new PasswordResetResponse(
                        "REQUEST_ACCEPTED",
                        "If the account is eligible for password recovery, "
                                + "the recovery process can continue.",
                        resetToken
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<PasswordResetResponse> verifyReset(
            @RequestBody PasswordResetVerifyRequest request
    ) {
        passwordResetService.verifyTotp(
                request.resetToken(),
                request.totpCode()
        );

        return ResponseEntity.ok(
                new PasswordResetResponse(
                        "VERIFIED",
                        "Identity verification successful.",
                        null
                )
        );
    }

    @PostMapping("/complete")
    public ResponseEntity<PasswordResetResponse> completeReset(
            @RequestBody PasswordResetCompleteRequest request
    ) {
        passwordResetService.completeReset(
                request.resetToken(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                new PasswordResetResponse(
                        "PASSWORD_RESET_SUCCESS",
                        "Password changed successfully.",
                        null
                )
        );
    }
}