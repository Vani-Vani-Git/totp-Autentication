package com.totp.auth.security;

import com.totp.auth.entity.AuthenticatedSession;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;
    private SessionValidationService sessionValidationService;
    private final Long userId = 100L;
    private final String userIdentifier = "user@example.com";
    private final UUID sessionId = UUID.randomUUID();
    private final UUID accessTokenJti = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        String secret =
                "VGhpc0lzQVNlY3VyZUpXVFNlY3JldEtleUZvckRldk9ubHk=";

        jwtService = new JwtService(
                secret,
                900L
        );

        sessionValidationService =
                mock(SessionValidationService.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                sessionValidationService
        );
    }
    private AuthenticatedSession createValidSession() {

        AuthenticatedSession session =
                new AuthenticatedSession();

        session.setAccessTokenJti(accessTokenJti);
        session.setSessionId(sessionId);

        session.setExpiresAt(
                java.time.LocalDateTime.now()
                        .plusMinutes(15)
        );

        return session;
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        verify(chain).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        verify(chain).doFilter(
                request,
                response
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void shouldAuthenticateValidJwt()
            throws Exception {
        AuthenticatedSession session =
                createValidSession();

        when(
                sessionValidationService.validateSession(
                        accessTokenJti
                )
        ).thenReturn(session);
        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        var authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                userIdentifier,
                authentication.getPrincipal()
        );

        assertTrue(
                authentication.isAuthenticated()
        );

        assertInstanceOf(
                JwtAuthenticationFilter.JwtAuthenticationDetails.class,
                authentication.getDetails()
        );

        var details =
                (JwtAuthenticationFilter.JwtAuthenticationDetails)
                        authentication.getDetails();

        assertEquals(
                accessTokenJti,
                details.accessTokenJti()
        );

        assertEquals(
                sessionId,
                details.sessionId()
        );

        verify(chain).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldRejectInvalidJwt()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid.jwt.token"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(chain).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldRejectTamperedJwt()
            throws Exception {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        String tamperedToken =
                token.substring(
                        0,
                        token.length() - 2
                ) + "xx";

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + tamperedToken
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(chain).doFilter(
                request,
                response
        );
    }

    @Test
    void shouldNotAuthenticateWhenSessionIsInvalid()
            throws Exception {

        String token =
                jwtService.generateAccessToken(
                        userId,
                        userIdentifier,
                        sessionId,
                        accessTokenJti
                );

        when(
                sessionValidationService.validateSession(
                        accessTokenJti
                )
        ).thenReturn(null);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(chain).doFilter(
                request,
                response
        );
    }
}