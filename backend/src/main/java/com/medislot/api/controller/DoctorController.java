package com.medislot.api.controller;

import com.medislot.api.domain.AppUser;
import com.medislot.api.dto.ApiResponse;
import com.medislot.api.dto.BookingDtos;
import com.medislot.api.dto.ProviderDtos;
import com.medislot.api.security.AuthenticatedUser;
import com.medislot.api.service.AppointmentService;
import com.medislot.api.service.AuthService;
import com.medislot.api.service.ProviderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final ProviderService providers; private final AppointmentService appointments; private final AuthService auth;
    public DoctorController(ProviderService providers, AppointmentService appointments, AuthService auth) { this.providers = providers; this.appointments = appointments; this.auth = auth; }
    @GetMapping public ApiResponse<Page<ProviderDtos.DoctorSummary>> search(@RequestParam(required = false) String city, @RequestParam(required = false, name = "q") String query, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) { return ApiResponse.of(providers.findDoctors(city, query, page, size)); }
    @GetMapping("/{id}") public ApiResponse<ProviderDtos.DoctorDetail> detail(@PathVariable UUID id) { return ApiResponse.of(providers.publicDoctor(id)); }
    @GetMapping("/{id}/slots") public ApiResponse<List<BookingDtos.SlotResponse>> slots(@PathVariable UUID id, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) { return ApiResponse.of(appointments.publicSlots(id, date)); }
    @GetMapping("/me") @PreAuthorize("hasRole('DOCTOR')") public ApiResponse<ProviderDtos.DoctorDetail> me(@AuthenticationPrincipal AuthenticatedUser user) { return ApiResponse.of(providers.myDoctor(user.id())); }
    @PutMapping("/me") @PreAuthorize("hasRole('DOCTOR')") public ApiResponse<ProviderDtos.DoctorDetail> update(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ProviderDtos.DoctorUpdateRequest request) { return ApiResponse.of(providers.updateDoctor(user.id(), request)); }
    @PostMapping("/me/slots") @PreAuthorize("hasRole('DOCTOR')") public ApiResponse<BookingDtos.SlotResponse> createSlot(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody BookingDtos.CreateSlotRequest request) { AppUser actor = auth.requireUser(user.id()); return ApiResponse.of(appointments.createSlot(actor, request)); }
    @GetMapping("/me/appointments") @PreAuthorize("hasRole('DOCTOR')") public ApiResponse<List<BookingDtos.AppointmentResponse>> appointments(@AuthenticationPrincipal AuthenticatedUser user) { return ApiResponse.of(appointments.doctorAppointments(user.id())); }
    @PatchMapping("/me/appointments/{id}") @PreAuthorize("hasRole('DOCTOR')") public ApiResponse<BookingDtos.AppointmentResponse> changeAppointment(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id, @Valid @RequestBody BookingDtos.ChangeAppointmentStatusRequest request) { return ApiResponse.of(appointments.doctorTransition(auth.requireUser(user.id()), id, request)); }
}
