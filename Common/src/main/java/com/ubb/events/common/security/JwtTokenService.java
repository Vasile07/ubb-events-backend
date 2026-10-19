package com.ubb.events.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);
    private final SecretKey key;

    public JwtTokenService(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) throw new IllegalStateException("JWT secret must be configured");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString())
                .expiration(Date.from(now.plus(TOKEN_LIFETIME)))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
