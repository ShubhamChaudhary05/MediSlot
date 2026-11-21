package com.medislot.api.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "specializations")
public class Specialization extends BaseEntity {
    @Id private UUID id = newId();
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false, unique = true) private String slug;
    @Column(nullable = false) private boolean active = true;
    protected Specialization() { }
    public Specialization(String name, String slug) { this.name = name; this.slug = slug; }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
