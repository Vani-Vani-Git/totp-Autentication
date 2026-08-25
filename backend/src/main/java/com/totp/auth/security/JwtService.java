package com.totp.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;

    public JwtService(
            @Value("${app.security.jwt-secret}") String base64Secret,
            @Value("${app.security.access-token-expiration-seconds}")
            long accessTokenExpirationSeconds
    ) {
        byte[] secretBytes;

        try {
            secretBytes = Decoders.BASE64.decode(base64Secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "TOTP_JWT_SECRET must be valid Base64",
                    exception
            );
        }

        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "TOTP_JWT_SECRET must decode to at least 32 bytes"
            );
        }

        this.signingKey =
                Keys.hmacShaKeyFor(secretBytes);

        this.accessTokenExpirationSeconds =
                accessTokenExpirationSeconds;
    }

    public String generateAccessToken(
            Long userId,
            String userIdentifier,
            UUID sessionId,
            UUID accessTokenJti
    ) {
        Instant issuedAt = Instant.now();

        Instant expiration =
                issuedAt.plusSeconds(
                        accessTokenExpirationSeconds
                );

        return Jwts.builder()
                .subject(userIdentifier)
                .claim("userId", userId)
                .claim("sessionId", sessionId.toString())
                .id(accessTokenJti.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    public Jws<Claims> parseAndValidate(
            String token
    ) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
    }

    public UUID extractAccessTokenJti(
            String token
    ) {
        String jti =
                parseAndValidate(token)
                        .getPayload()
                        .getId();

        return UUID.fromString(jti);
    }

    public UUID extractSessionId(
            String token
    ) {
        String sessionId =
                parseAndValidate(token)
                        .getPayload()
                        .get("sessionId", String.class);

        return UUID.fromString(sessionId);
    }

    public String extractSubject(
            String token
    ) {
        return parseAndValidate(token)
                .getPayload()
                .getSubject();
    }
}