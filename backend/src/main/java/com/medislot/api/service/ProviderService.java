package com.medislot.api.service;

import com.medislot.api.domain.*;
import com.medislot.api.dto.ProviderDtos;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.DoctorProfileRepository;
import com.medislot.api.repository.HospitalProfileRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderService {
    private final DoctorProfileRepository doctors;
    private final HospitalProfileRepository hospitals;
    private final AuditService audit;
    public ProviderService(DoctorProfileRepository doctors, HospitalProfileRepository hospitals, AuditService audit) { this.doctors = doctors; this.hospitals = hospitals; this.audit = audit; }
    @Transactional(readOnly = true)
    public Page<ProviderDtos.DoctorSummary> findDoctors(String city, String query, int page, int size) {
        return doctors.searchApproved(ProviderStatus.APPROVED, blankToEmpty(city), blankToEmpty(query), PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50))).map(this::doctorSummary);
    }
    @Transactional(readOnly = true)
    public ProviderDtos.DoctorDetail publicDoctor(UUID id) { return doctorDetail(requireApprovedDoctor(id)); }
    @Transactional(readOnly = true)
    public Page<ProviderDtos.HospitalSummary> findHospitals(String city, String query, int page, int size) {
        return hospitals.searchApproved(ProviderStatus.APPROVED, blankToEmpty(city), blankToEmpty(query), PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50))).map(this::hospitalSummary);
    }
    @Transactional(readOnly = true)
    public Page<ProviderDtos.DoctorSummary> adminDoctors(ProviderStatus status, int page, int size) {
        return doctors.findByStatus(status, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50))).map(this::doctorSummary);
    }
    @Transactional(readOnly = true)
    public Page<ProviderDtos.HospitalSummary> adminHospitals(ProviderStatus status, int page, int size) {
        return hospitals.findByStatus(status, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50))).map(this::hospitalSummary);
    }
    @Transactional(readOnly = true)
    public ProviderDtos.HospitalDetail publicHospital(UUID id) { return hospitalDetail(requireApprovedHospital(id)); }
    @Transactional(readOnly = true)
    public ProviderDtos.DoctorDetail myDoctor(UUID userId) { return doctorDetail(requireDoctorOwner(userId)); }
    @Transactional
    public ProviderDtos.DoctorDetail updateDoctor(UUID userId, ProviderDtos.DoctorUpdateRequest request) {
        DoctorProfile doctor = requireDoctorOwner(userId);
        doctor.update(request.qualification(), request.biography(), request.yearsOfExperience(), request.consultationFee(), request.city(), request.address(), request.profileImageUrl());
        return doctorDetail(doctor);
    }
    @Transactional(readOnly = true)
    public ProviderDtos.HospitalDetail myHospital(UUID userId) { return hospitalDetail(requireHospitalOwner(userId)); }
    @Transactional
    public ProviderDtos.HospitalDetail updateHospital(UUID userId, ProviderDtos.HospitalUpdateRequest request) {
        HospitalProfile hospital = requireHospitalOwner(userId);
        hospital.update(request.facilityName(), request.phone(), request.address(), request.city(), request.description());
        return hospitalDetail(hospital);
    }
    @Transactional
    public void decideDoctor(AppUser admin, UUID id, ProviderDtos.ProviderDecisionRequest request) {
        if (request.status() != ProviderStatus.APPROVED && request.status() != ProviderStatus.REJECTED && request.status() != ProviderStatus.SUSPENDED) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Providers may be approved, rejected, or suspended.");
        if (request.status() == ProviderStatus.REJECTED && (request.reason() == null || request.reason().isBlank())) throw new ApiException(HttpStatus.BAD_REQUEST, "REASON_REQUIRED", "A rejection reason is required.");
        DoctorProfile doctor = doctors.findById(id).orElseThrow(() -> notFound("Doctor"));
        doctor.setStatus(request.status(), request.reason());
        audit.record(admin, "DOCTOR_" + request.status(), "DOCTOR", id, request.reason());
    }
    @Transactional
    public void decideHospital(AppUser admin, UUID id, ProviderDtos.ProviderDecisionRequest request) {
        if (request.status() != ProviderStatus.APPROVED && request.status() != ProviderStatus.REJECTED && request.status() != ProviderStatus.SUSPENDED) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Providers may be approved, rejected, or suspended.");
        if (request.status() == ProviderStatus.REJECTED && (request.reason() == null || request.reason().isBlank())) throw new ApiException(HttpStatus.BAD_REQUEST, "REASON_REQUIRED", "A rejection reason is required.");
        HospitalProfile hospital = hospitals.findById(id).orElseThrow(() -> notFound("Hospital"));
        hospital.setStatus(request.status(), request.reason());
        audit.record(admin, "HOSPITAL_" + request.status(), "HOSPITAL", id, request.reason());
    }
    public DoctorProfile requireDoctorOwner(UUID userId) { return doctors.findByUserId(userId).orElseThrow(() -> notFound("Doctor profile")); }
    public HospitalProfile requireHospitalOwner(UUID userId) { return hospitals.findByUserId(userId).orElseThrow(() -> notFound("Hospital profile")); }
    public DoctorProfile requireApprovedDoctor(UUID id) { DoctorProfile doctor = doctors.findById(id).orElseThrow(() -> notFound("Doctor")); if (doctor.getStatus() != ProviderStatus.APPROVED) throw notFound("Doctor"); return doctor; }
    public HospitalProfile requireApprovedHospital(UUID id) { HospitalProfile hospital = hospitals.findById(id).orElseThrow(() -> notFound("Hospital")); if (hospital.getStatus() != ProviderStatus.APPROVED) throw notFound("Hospital"); return hospital; }
    public ProviderDtos.DoctorSummary doctorSummary(DoctorProfile doctor) { return new ProviderDtos.DoctorSummary(doctor.getId(), doctor.getUser().getDisplayName(), doctor.getQualification(), doctor.getYearsOfExperience(), doctor.getConsultationFee(), doctor.getCity(), doctor.getProfileImageUrl(), doctor.getStatus()); }
    public ProviderDtos.DoctorDetail doctorDetail(DoctorProfile doctor) { return new ProviderDtos.DoctorDetail(doctor.getId(), doctor.getUser().getDisplayName(), doctor.getQualification(), doctor.getBiography(), doctor.getYearsOfExperience(), doctor.getConsultationFee(), doctor.getCity(), doctor.getAddress(), doctor.getProfileImageUrl(), doctor.getStatus()); }
    private ProviderDtos.HospitalSummary hospitalSummary(HospitalProfile hospital) { return new ProviderDtos.HospitalSummary(hospital.getId(), hospital.getFacilityName(), hospital.getCity(), hospital.getAddress(), hospital.getStatus()); }
    private ProviderDtos.HospitalDetail hospitalDetail(HospitalProfile hospital) { return new ProviderDtos.HospitalDetail(hospital.getId(), hospital.getFacilityName(), hospital.getRegistrationNumber(), hospital.getPhone(), hospital.getCity(), hospital.getAddress(), hospital.getDescription(), hospital.getStatus()); }
    private static String blankToEmpty(String value) { return value == null || value.isBlank() ? "" : value.trim(); }
    private static ApiException notFound(String type) { return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", type + " was not found."); }
}
