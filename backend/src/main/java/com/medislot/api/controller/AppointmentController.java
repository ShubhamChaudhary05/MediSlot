package com.medislot.api.controller;

import com.medislot.api.dto.ApiResponse;
import com.medislot.api.dto.BookingDtos;
import com.medislot.api.security.AuthenticatedUser;
import com.medislot.api.service.AppointmentService;
import com.medislot.api.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointments; private final AuthService auth;
    public AppointmentController(AppointmentService appointments, AuthService auth) { this.appointments = appointments; this.auth = auth; }
    @PostMapping @PreAuthorize("hasRole('PATIENT')") public ApiResponse<BookingDtos.AppointmentResponse> book(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody BookingDtos.BookAppointmentRequest request) { return ApiResponse.of(appointments.book(auth.requireUser(user.id()), request)); }
    @GetMapping @PreAuthorize("hasRole('PATIENT')") public ApiResponse<List<BookingDtos.AppointmentResponse>> mine(@AuthenticationPrincipal AuthenticatedUser user) { return ApiResponse.of(appointments.patientAppointments(user.id())); }
    @PostMapping("/{id}/cancel") @PreAuthorize("hasRole('PATIENT')") public ApiResponse<BookingDtos.AppointmentResponse> cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id, @Valid @RequestBody BookingDtos.CancelAppointmentRequest request) { return ApiResponse.of(appointments.cancel(auth.requireUser(user.id()), id, request)); }
}
