package com.ubb.events.common.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {
    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing";

    @Test
    void issuesHs256TokenWithOnlyIdentityAndExpiryFor24Hours() {
        JwtTokenService service = new JwtTokenService(SECRET);
        Instant before = Instant.now();
        String token = service.issue(42L);
        var claims = Jwts.parser().verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build()
                .parseSignedClaims(token).getPayload();
        assertEquals("42", claims.getSubject());
        assertNotNull(claims.getExpiration());
        assertEquals(2, claims.size());
        assertTrue(claims.getExpiration().toInstant().isAfter(before.plus(Duration.ofHours(23))));
        assertTrue(claims.getExpiration().toInstant().isBefore(before.plus(Duration.ofHours(25))));
        assertEquals("HS256", Jwts.parser().verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build()
                .parseSignedClaims(token).getHeader().getAlgorithm());
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenService service = new JwtTokenService(SECRET);
        String expired = Jwts.builder().subject("42").expiration(Date.from(Instant.now().minusSeconds(1)))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
        assertThrows(RuntimeException.class, () -> service.parseUserId(expired));
    }
}
