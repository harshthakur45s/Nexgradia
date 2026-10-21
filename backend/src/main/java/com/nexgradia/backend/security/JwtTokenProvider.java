package com.nexgradia.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    public static final String DEFAULT_DEV_SECRET =
            "nexgradia_default_development_secret_key_needs_to_be_at_least_32_bytes_long_for_hmac_sha256";

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs,
            Environment environment) {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret key must be at least 32 bytes (256 bits) for HMAC-SHA256 signature security.");
        }

        if (environment != null) {
            List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
            if ((activeProfiles.contains("prod") || activeProfiles.contains("production"))
                    && DEFAULT_DEV_SECRET.equals(jwtSecret)) {
                throw new IllegalStateException(
                        "Production environment must not use the default development JWT secret. Set the JWT_SECRET environment variable.");
            }
        }

        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(UserPrincipal userPrincipal) {
        return generateTokenWithCustomExpiration(userPrincipal, jwtExpirationMs);
    }

    public String generateTokenWithCustomExpiration(UserPrincipal userPrincipal, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("id", userPrincipal.getId())
                .claim("roles", userPrincipal.getAuthorities().stream().map(Object::toString).toList())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }
}
