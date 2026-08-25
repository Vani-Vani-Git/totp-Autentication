package com.totp.auth.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

@Service
public class TotpService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int DEFAULT_DIGITS = 6;
    private static final long DEFAULT_PERIOD_SECONDS = 30L;

    private final Base32Codec base32Codec;

    public TotpService(Base32Codec base32Codec) {
        this.base32Codec = base32Codec;
    }

    public String generateCode(String secret) {
        return generateCode(
                secret,
                System.currentTimeMillis() / 1000L,
                DEFAULT_PERIOD_SECONDS,
                DEFAULT_DIGITS
        );
    }

    public String generateCode(
            String secret,
            long unixTimeSeconds,
            long periodSeconds,
            int digits
    ) {
        validateInputs(
                secret,
                periodSeconds,
                digits
        );

        long timeStep = unixTimeSeconds / periodSeconds;

        byte[] secretBytes = base32Codec.decode(secret);

        byte[] counter = ByteBuffer
                .allocate(Long.BYTES)
                .putLong(timeStep)
                .array();

        byte[] hash = calculateHmac(
                secretBytes,
                counter
        );

        int offset = hash[hash.length - 1] & 0x0F;

        int binaryCode =
                ((hash[offset] & 0x7F) << 24)
                        | ((hash[offset + 1] & 0xFF) << 16)
                        | ((hash[offset + 2] & 0xFF) << 8)
                        | (hash[offset + 3] & 0xFF);

        int modulus = powerOfTen(digits);

        int otp = binaryCode % modulus;

        return String.format(
                "%0" + digits + "d",
                otp
        );
    }

    private byte[] calculateHmac(
            byte[] secret,
            byte[] counter
    ) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec key = new SecretKeySpec(
                    secret,
                    HMAC_ALGORITHM
            );

            mac.init(key);

            return mac.doFinal(counter);

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to calculate TOTP HMAC",
                    exception
            );
        }
    }

    private int powerOfTen(int digits) {
        int result = 1;

        for (int i = 0; i < digits; i++) {
            result *= 10;
        }

        return result;
    }

    private void validateInputs(
            String secret,
            long periodSeconds,
            int digits
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "TOTP secret cannot be null or empty"
            );
        }

        if (periodSeconds <= 0) {
            throw new IllegalArgumentException(
                    "TOTP period must be greater than zero"
            );
        }

        if (digits < 6 || digits > 8) {
            throw new IllegalArgumentException(
                    "TOTP digits must be between 6 and 8"
            );
        }
    }

    private boolean constantTimeEquals(
            String expected,
            String supplied
    ) {
        if (expected == null || supplied == null) {
            return false;
        }

        byte[] expectedBytes =
                expected.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        byte[] suppliedBytes =
                supplied.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return java.security.MessageDigest.isEqual(
                expectedBytes,
                suppliedBytes
        );
    }

    public boolean verifyCode(
            String secret,
            String suppliedCode,
            long unixTimeSeconds
    ) {
        if (suppliedCode == null || suppliedCode.isBlank()) {
            return false;
        }

        for (long offset = -1; offset <= 1; offset++) {

            long verificationTime =
                    unixTimeSeconds + (offset * DEFAULT_PERIOD_SECONDS);

            String expectedCode = generateCode(
                    secret,
                    verificationTime,
                    DEFAULT_PERIOD_SECONDS,
                    DEFAULT_DIGITS
            );

            if (constantTimeEquals(
                    expectedCode,
                    suppliedCode
            )) {
                return true;
            }
        }

        return false;
    }
}