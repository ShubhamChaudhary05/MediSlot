package com.medislot.api.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser extends BaseEntity {
    @Id
    private UUID id = newId();
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;
    @Column(name = "auth_version", nullable = false)
    private int authVersion;

    protected AppUser() { }
    public AppUser(String email, String passwordHash, String displayName, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public UserRole getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public int getAuthVersion() { return authVersion; }
    public void invalidateSessions() { authVersion++; }
}
