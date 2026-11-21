package com.medislot.api.controller;

import com.medislot.api.domain.AppUser;
import com.medislot.api.domain.UserRole;
import com.medislot.api.domain.ProviderStatus;
import com.medislot.api.dto.ApiResponse;
import com.medislot.api.dto.AuthDtos;
import com.medislot.api.dto.ProviderDtos;
import com.medislot.api.repository.AppointmentRepository;
import com.medislot.api.repository.DoctorProfileRepository;
import com.medislot.api.repository.HospitalProfileRepository;
import com.medislot.api.repository.UserRepository;
import com.medislot.api.security.AuthenticatedUser;
import com.medislot.api.service.AuthService;
import com.medislot.api.service.ProviderService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ProviderService providers; private final AuthService auth; private final UserRepository users; private final DoctorProfileRepository doctors; private final HospitalProfileRepository hospitals; private final AppointmentRepository appointments;
    public AdminController(ProviderService providers, AuthService auth, UserRepository users, DoctorProfileRepository doctors, HospitalProfileRepository hospitals, AppointmentRepository appointments) { this.providers = providers; this.auth = auth; this.users = users; this.doctors = doctors; this.hospitals = hospitals; this.appointments = appointments; }
    @GetMapping("/dashboard") public ApiResponse<PlatformStats> dashboard() { return ApiResponse.of(new PlatformStats(users.count(), doctors.count(), hospitals.count(), appointments.count())); }
    @GetMapping("/users") public ApiResponse<Page<AuthDtos.UserSummary>> users(@RequestParam UserRole role, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) { return ApiResponse.of(users.findByRole(role, PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)))).map(user -> auth.currentUser(user.getId()))); }
    @GetMapping("/doctors") public ApiResponse<Page<ProviderDtos.DoctorSummary>> doctors(@RequestParam(defaultValue = "PENDING") ProviderStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) { return ApiResponse.of(providers.adminDoctors(status, page, size)); }
    @GetMapping("/hospitals") public ApiResponse<Page<ProviderDtos.HospitalSummary>> hospitals(@RequestParam(defaultValue = "PENDING") ProviderStatus status, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) { return ApiResponse.of(providers.adminHospitals(status, page, size)); }
    @PutMapping("/doctors/{id}/status") public ApiResponse<Void> decideDoctor(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id, @Valid @RequestBody ProviderDtos.ProviderDecisionRequest request) { providers.decideDoctor(auth.requireUser(principal.id()), id, request); return ApiResponse.of(null); }
    @PutMapping("/hospitals/{id}/status") public ApiResponse<Void> decideHospital(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id, @Valid @RequestBody ProviderDtos.ProviderDecisionRequest request) { providers.decideHospital(auth.requireUser(principal.id()), id, request); return ApiResponse.of(null); }
    public record PlatformStats(long users, long doctors, long hospitals, long appointments) { }
}
