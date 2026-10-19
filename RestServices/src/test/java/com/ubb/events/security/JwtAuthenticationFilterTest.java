package com.ubb.events.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubb.events.common.security.JwtTokenService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {
    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing";
    private final JwtTokenService tokenService = new JwtTokenService(SECRET);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenService, new ObjectMapper());

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    @Test
    void validBearerTokenSetsCurrentUserPrincipal() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenService.issue(42L));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(42L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals(200, response.getStatus());
    }

    @Test
    void malformedTamperedAndExpiredTokensReturnSharedUnauthorizedResponse() throws Exception {
        String expired = Jwts.builder().subject("42").expiration(Date.from(Instant.now().minusSeconds(1)))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
        for (String token : new String[]{"malformed", tokenService.issue(42L) + "tampered", expired}) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + token);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, new MockFilterChain());

            assertEquals(401, response.getStatus());
            assertEquals("application/json", response.getContentType());
            assertEquals(401, new ObjectMapper().readTree(response.getContentAsString()).get("status").asInt());
            assertEquals("Invalid or expired token", new ObjectMapper().readTree(response.getContentAsString()).get("message").asText());
            SecurityContextHolder.clearContext();
        }
    }
}
