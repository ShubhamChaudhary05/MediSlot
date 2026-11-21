package com.medislot.api.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "doctor_profiles")
public class DoctorProfile extends BaseEntity {
    @Id private UUID id = newId();
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
    @Column(name = "registration_number", nullable = false, unique = true) private String registrationNumber;
    private String qualification;
    @Column(columnDefinition = "text") private String biography;
    @Column(nullable = false) private int yearsOfExperience;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal consultationFee = BigDecimal.ZERO;
    private String city;
    private String address;
    private String profileImageUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ProviderStatus status = ProviderStatus.PENDING;
    private String rejectionReason;
    protected DoctorProfile() { }
    public DoctorProfile(AppUser user, String registrationNumber) { this.user = user; this.registrationNumber = registrationNumber; }
    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getQualification() { return qualification; }
    public String getBiography() { return biography; }
    public int getYearsOfExperience() { return yearsOfExperience; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public ProviderStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public void update(String qualification, String biography, int experience, BigDecimal fee, String city, String address, String imageUrl) {
        this.qualification = qualification; this.biography = biography; this.yearsOfExperience = experience; this.consultationFee = fee; this.city = city; this.address = address; this.profileImageUrl = imageUrl;
    }
    public void setStatus(ProviderStatus status, String rejectionReason) { this.status = status; this.rejectionReason = rejectionReason; }
}
