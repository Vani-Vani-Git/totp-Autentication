package com.totp.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordRecoveryEmailService {

    private final JavaMailSender mailSender;

    public PasswordRecoveryEmailService(
            JavaMailSender mailSender
    ) {
        this.mailSender = mailSender;
    }

    public void sendRecoveryCode(
            String recipientEmail,
            String code
    ) {
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(recipientEmail);
        message.setSubject(
                "Password Recovery Verification Code"
        );

        message.setText(
                "Your password recovery verification code is: "
                        + code
                        + "\n\n"
                        + "This code will expire in 10 minutes."
                        + "\n\n"
                        + "If you did not request a password recovery, "
                        + "you can safely ignore this email."
        );

        mailSender.send(message);
    }
}