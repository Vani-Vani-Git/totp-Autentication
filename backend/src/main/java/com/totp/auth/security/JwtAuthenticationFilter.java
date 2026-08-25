package com.totp.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessionValidationService sessionValidationService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            SessionValidationService sessionValidationService
    ) {
        this.jwtService = jwtService;
        this.sessionValidationService =
                sessionValidationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Jws<Claims> parsedToken =
                    jwtService.parseAndValidate(token);

            Claims claims =
                    parsedToken.getPayload();

            String subject =
                    claims.getSubject();

            String sessionId =
                    claims.get("sessionId", String.class);

            UUID accessTokenJti =
                    UUID.fromString(claims.getId());

            var session =
                    sessionValidationService.validateSession(
                            accessTokenJti
                    );

            if (session == null) {
                SecurityContextHolder
                        .clearContext();

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }

            if (subject == null
                    || subject.isBlank()
                    || sessionId == null
                    || sessionId.isBlank()) {

                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            subject,
                            null,
                            Collections.emptyList()
                    );

            authentication.setDetails(
                    new JwtAuthenticationDetails(
                            accessTokenJti,
                            UUID.fromString(sessionId)
                    )
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception exception) {

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(request, response);
    }

    public record JwtAuthenticationDetails(
            UUID accessTokenJti,
            UUID sessionId
    ) {
    }
}