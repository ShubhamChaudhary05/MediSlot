package com.medislot.api.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {
    @Id private UUID id = newId();
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "slot_id", nullable = false)
    private AppointmentSlot slot;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AppointmentStatus status = AppointmentStatus.BOOKED;
    private String reason;
    private String cancellationReason;
    protected Appointment() { }
    public Appointment(AppointmentSlot slot, PatientProfile patient, DoctorProfile doctor, String reason) { this.slot = slot; this.patient = patient; this.doctor = doctor; this.reason = reason; }
    public UUID getId() { return id; }
    public AppointmentSlot getSlot() { return slot; }
    public PatientProfile getPatient() { return patient; }
    public DoctorProfile getDoctor() { return doctor; }
    public AppointmentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public String getCancellationReason() { return cancellationReason; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public void cancel(String cancellationReason) { this.status = AppointmentStatus.CANCELLED; this.cancellationReason = cancellationReason; }
}
