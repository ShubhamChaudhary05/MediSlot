package com.medislot.api.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id private UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(nullable = false) private Instant expiresAt;
    private Instant usedAt;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected PasswordResetToken() { }
    public PasswordResetToken(AppUser user, String tokenHash, Instant expiresAt) { this.user = user; this.tokenHash = tokenHash; this.expiresAt = expiresAt; }
    public AppUser getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void markUsed() { usedAt = Instant.now(); }
}
