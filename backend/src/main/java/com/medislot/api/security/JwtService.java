package com.medislot.api.security;

import com.medislot.api.config.AppProperties;
import com.medislot.api.domain.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final AppProperties properties;
    public JwtService(AppProperties properties) { this.properties = properties; }
    public IssuedToken issue(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getSecurity().getAccessTokenMinutes() * 60);
        String tokenId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("authVersion", user.getAuthVersion())
                .id(tokenId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
        return new IssuedToken(token, tokenId, expiresAt);
    }
    public ParsedToken parse(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
        Integer authVersion = claims.get("authVersion", Integer.class);
        return new ParsedToken(UUID.fromString(claims.getSubject()), claims.get("email", String.class),
                claims.get("role", String.class), authVersion == null ? 0 : authVersion, claims.getId(), claims.getExpiration().toInstant());
    }
    private SecretKey signingKey() {
        String configured = properties.getSecurity().getJwtSecret();
        if (configured == null || configured.isBlank() || configured.startsWith("REPLACE_")) {
            throw new IllegalStateException("JWT_SECRET is not configured.");
        }
        try {
            byte[] bytes = Decoders.BASE64.decode(configured);
            if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes.");
            return Keys.hmacShaKeyFor(bytes);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be a Base64-encoded secret of at least 32 bytes.");
        }
    }
    public record IssuedToken(String value, String tokenId, Instant expiresAt) { }
    public record ParsedToken(UUID userId, String email, String role, int authVersion, String tokenId, Instant expiresAt) { }
}
