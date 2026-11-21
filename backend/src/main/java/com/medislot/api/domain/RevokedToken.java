package com.medislot.api.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {
    @Id private UUID id = UUID.randomUUID();
    @Column(name = "token_id", nullable = false, unique = true) private String tokenId;
    @Column(nullable = false) private Instant expiresAt;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected RevokedToken() { }
    public RevokedToken(String tokenId, Instant expiresAt) { this.tokenId = tokenId; this.expiresAt = expiresAt; }
    public String getTokenId() { return tokenId; }
    public Instant getExpiresAt() { return expiresAt; }
}
