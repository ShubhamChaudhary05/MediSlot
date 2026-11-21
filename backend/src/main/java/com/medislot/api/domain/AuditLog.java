package com.medislot.api.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id private UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_user_id") private AppUser actorUser;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String entityType;
    private UUID entityId;
    private String detail;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected AuditLog() { }
    public AuditLog(AppUser actorUser, String action, String entityType, UUID entityId, String detail) { this.actorUser = actorUser; this.action = action; this.entityType = entityType; this.entityId = entityId; this.detail = detail; }
}
