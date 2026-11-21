package com.medislot.api.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patient_profiles")
public class PatientProfile extends BaseEntity {
    @Id private UUID id = newId();
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String emergencyContact;
    protected PatientProfile() { }
    public PatientProfile(AppUser user) { this.user = user; }
    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getPhone() { return phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getEmergencyContact() { return emergencyContact; }
    public void update(String phone, LocalDate dateOfBirth, String gender, String emergencyContact) {
        this.phone = phone; this.dateOfBirth = dateOfBirth; this.gender = gender; this.emergencyContact = emergencyContact;
    }
}
