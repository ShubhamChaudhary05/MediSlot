package com.medislot.api.dto;

import com.medislot.api.domain.ProviderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class ProviderDtos {
    private ProviderDtos() { }
    public record DoctorUpdateRequest(
        @Size(max = 500) String qualification, @Size(max = 5000) String biography,
        @Max(80) int yearsOfExperience, @NotNull @DecimalMin("0.00") BigDecimal consultationFee,
        @Size(max = 120) String city, @Size(max = 500) String address, @Size(max = 2048) String profileImageUrl
    ) { }
    public record HospitalUpdateRequest(
        @NotBlank @Size(max = 180) String facilityName, @Size(max = 32) String phone,
        @Size(max = 500) String address, @Size(max = 120) String city, @Size(max = 5000) String description
    ) { }
    public record DoctorSummary(UUID id, String name, String qualification, int yearsOfExperience, BigDecimal consultationFee, String city, String imageUrl, ProviderStatus status) { }
    public record DoctorDetail(UUID id, String name, String qualification, String biography, int yearsOfExperience, BigDecimal consultationFee, String city, String address, String imageUrl, ProviderStatus status) { }
    public record HospitalSummary(UUID id, String name, String city, String address, ProviderStatus status) { }
    public record HospitalDetail(UUID id, String name, String registrationNumber, String phone, String city, String address, String description, ProviderStatus status) { }
    public record ProviderDecisionRequest(@NotNull ProviderStatus status, @Size(max = 500) String reason) { }
}
