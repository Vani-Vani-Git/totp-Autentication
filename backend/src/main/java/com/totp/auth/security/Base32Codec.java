package com.totp.auth.security;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class Base32Codec {

    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public byte[] decode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Base32 value cannot be null or empty"
            );
        }

        String normalized = normalize(value);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        int buffer = 0;
        int bitsInBuffer = 0;

        for (char character : normalized.toCharArray()) {
            int valueIndex = ALPHABET.indexOf(character);

            if (valueIndex < 0) {
                throw new IllegalArgumentException(
                        "Invalid Base32 character: " + character
                );
            }

            buffer = (buffer << 5) | valueIndex;
            bitsInBuffer += 5;

            if (bitsInBuffer >= 8) {
                bitsInBuffer -= 8;

                int decodedByte =
                        (buffer >> bitsInBuffer) & 0xFF;

                output.write(decodedByte);
            }
        }

        return output.toByteArray();
    }

    public String encode(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException(
                    "Data cannot be null or empty"
            );
        }

        StringBuilder result = new StringBuilder();

        int buffer = 0;
        int bitsInBuffer = 0;

        for (byte currentByte : data) {
            buffer = (buffer << 8) | (currentByte & 0xFF);
            bitsInBuffer += 8;

            while (bitsInBuffer >= 5) {
                bitsInBuffer -= 5;

                int index =
                        (buffer >> bitsInBuffer) & 0x1F;

                result.append(ALPHABET.charAt(index));
            }
        }

        if (bitsInBuffer > 0) {
            int index =
                    (buffer << (5 - bitsInBuffer)) & 0x1F;

            result.append(ALPHABET.charAt(index));
        }

        return result.toString();
    }

    private String normalize(String value) {
        String normalized = value
                .trim()
                .replace(" ", "")
                .replace("-", "")
                .toUpperCase();

        while (normalized.endsWith("=")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized;
    }
}