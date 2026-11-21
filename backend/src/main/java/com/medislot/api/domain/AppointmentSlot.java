package com.medislot.api.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment_slots", uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "starts_at"}))
public class AppointmentSlot extends BaseEntity {
    @Id private UUID id = newId();
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;
    @Column(nullable = false) private Instant startsAt;
    @Column(nullable = false) private Instant endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SlotStatus status = SlotStatus.AVAILABLE;
    @Version private long version;
    protected AppointmentSlot() { }
    public AppointmentSlot(DoctorProfile doctor, Instant startsAt, Instant endsAt) { this.doctor = doctor; this.startsAt = startsAt; this.endsAt = endsAt; }
    public UUID getId() { return id; }
    public DoctorProfile getDoctor() { return doctor; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
}
