package com.medislot.api.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "hospital_profiles")
public class HospitalProfile extends BaseEntity {
    @Id private UUID id = newId();
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
    @Column(nullable = false) private String facilityName;
    @Column(nullable = false, unique = true) private String registrationNumber;
    private String phone;
    private String address;
    private String city;
    @Column(columnDefinition = "text") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ProviderStatus status = ProviderStatus.PENDING;
    private String rejectionReason;
    protected HospitalProfile() { }
    public HospitalProfile(AppUser user, String facilityName, String registrationNumber) { this.user = user; this.facilityName = facilityName; this.registrationNumber = registrationNumber; }
    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getFacilityName() { return facilityName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getDescription() { return description; }
    public ProviderStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public void update(String facilityName, String phone, String address, String city, String description) { this.facilityName = facilityName; this.phone = phone; this.address = address; this.city = city; this.description = description; }
    public void setStatus(ProviderStatus status, String rejectionReason) { this.status = status; this.rejectionReason = rejectionReason; }
}
