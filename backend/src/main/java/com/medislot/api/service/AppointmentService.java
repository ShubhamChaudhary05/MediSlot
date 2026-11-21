package com.medislot.api.service;

import com.medislot.api.domain.*;
import com.medislot.api.dto.BookingDtos;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final AppointmentSlotRepository slots;
    private final AppointmentRepository appointments;
    private final PatientProfileRepository patients;
    private final ProviderService providers;
    private final AuditService audit;
    public AppointmentService(AppointmentSlotRepository slots, AppointmentRepository appointments, PatientProfileRepository patients, ProviderService providers, AuditService audit) {
        this.slots = slots; this.appointments = appointments; this.patients = patients; this.providers = providers; this.audit = audit;
    }
    @Transactional
    public BookingDtos.SlotResponse createSlot(AppUser user, BookingDtos.CreateSlotRequest request) {
        DoctorProfile doctor = providers.requireDoctorOwner(user.getId());
        if (!request.endsAt().isAfter(request.startsAt()) || Duration.between(request.startsAt(), request.endsAt()).toMinutes() > 180) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SLOT", "A slot must end after it starts and be no longer than three hours.");
        if (slots.existsByDoctorIdAndStartsAt(doctor.getId(), request.startsAt())) throw new ApiException(HttpStatus.CONFLICT, "SLOT_EXISTS", "A slot already starts at that time.");
        AppointmentSlot slot = slots.save(new AppointmentSlot(doctor, request.startsAt(), request.endsAt()));
        audit.record(user, "SLOT_CREATED", "APPOINTMENT_SLOT", slot.getId(), null);
        return slotResponse(slot);
    }
    @Transactional(readOnly = true)
    public List<BookingDtos.SlotResponse> publicSlots(UUID doctorId, LocalDate date) {
        providers.requireApprovedDoctor(doctorId);
        Instant start = (date == null ? LocalDate.now(ZoneOffset.UTC) : date).atStartOfDay().toInstant(ZoneOffset.UTC);
        return slots.findByDoctorIdAndStatusAndStartsAtBetweenOrderByStartsAtAsc(doctorId, SlotStatus.AVAILABLE, start, start.plus(Duration.ofDays(1))).stream()
            .filter(slot -> slot.getStartsAt().isAfter(Instant.now())).map(this::slotResponse).toList();
    }
    @Transactional
    public BookingDtos.AppointmentResponse book(AppUser user, BookingDtos.BookAppointmentRequest request) {
        PatientProfile patient = patients.findByUserId(user.getId()).orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "PROFILE_MISSING", "Your patient profile is not available."));
        AppointmentSlot slot = slots.findForBookingById(request.slotId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Appointment slot was not found."));
        if (slot.getStatus() != SlotStatus.AVAILABLE || !slot.getStartsAt().isAfter(Instant.now())) throw new ApiException(HttpStatus.CONFLICT, "SLOT_UNAVAILABLE", "That appointment slot is no longer available.");
        if (slot.getDoctor().getStatus() != ProviderStatus.APPROVED) throw new ApiException(HttpStatus.CONFLICT, "PROVIDER_UNAVAILABLE", "This doctor is not available for booking.");
        slot.setStatus(SlotStatus.BOOKED);
        try {
            Appointment appointment = appointments.saveAndFlush(new Appointment(slot, patient, slot.getDoctor(), request.reason()));
            audit.record(user, "APPOINTMENT_BOOKED", "APPOINTMENT", appointment.getId(), null);
            return appointmentResponse(appointment);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "SLOT_UNAVAILABLE", "That appointment slot is no longer available.");
        }
    }
    @Transactional(readOnly = true)
    public List<BookingDtos.AppointmentResponse> patientAppointments(UUID userId) {
        PatientProfile patient = patients.findByUserId(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Patient profile was not found."));
        return appointments.findByPatientIdOrderByCreatedAtDesc(patient.getId()).stream().map(this::appointmentResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<BookingDtos.AppointmentResponse> doctorAppointments(UUID userId) {
        DoctorProfile doctor = providers.requireDoctorOwner(userId);
        return appointments.findByDoctorIdOrderByCreatedAtDesc(doctor.getId()).stream().map(this::appointmentResponse).toList();
    }
    @Transactional
    public BookingDtos.AppointmentResponse cancel(AppUser user, UUID id, BookingDtos.CancelAppointmentRequest request) {
        PatientProfile patient = patients.findByUserId(user.getId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Patient profile was not found."));
        Appointment appointment = appointments.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Appointment was not found."));
        if (!appointment.getPatient().getId().equals(patient.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot change this appointment.");
        if (appointment.getStatus() != AppointmentStatus.BOOKED && appointment.getStatus() != AppointmentStatus.CONFIRMED) throw new ApiException(HttpStatus.CONFLICT, "INVALID_TRANSITION", "This appointment cannot be cancelled.");
        if (!appointment.getSlot().getStartsAt().isAfter(Instant.now())) throw new ApiException(HttpStatus.CONFLICT, "INVALID_TRANSITION", "Past appointments cannot be cancelled.");
        appointment.cancel(request.reason());
        appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
        audit.record(user, "APPOINTMENT_CANCELLED", "APPOINTMENT", id, request.reason());
        return appointmentResponse(appointment);
    }
    @Transactional
    public BookingDtos.AppointmentResponse doctorTransition(AppUser user, UUID id, BookingDtos.ChangeAppointmentStatusRequest request) {
        DoctorProfile doctor = providers.requireDoctorOwner(user.getId());
        Appointment appointment = appointments.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Appointment was not found."));
        if (!appointment.getDoctor().getId().equals(doctor.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot change this appointment.");
        AppointmentStatus current = appointment.getStatus();
        AppointmentStatus next = request.status();
        boolean valid = (current == AppointmentStatus.BOOKED && (next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.REJECTED)) || (current == AppointmentStatus.CONFIRMED && next == AppointmentStatus.COMPLETED);
        if (!valid) throw new ApiException(HttpStatus.CONFLICT, "INVALID_TRANSITION", "That appointment status transition is not allowed.");
        appointment.setStatus(next);
        if (next == AppointmentStatus.REJECTED) appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
        audit.record(user, "APPOINTMENT_" + next, "APPOINTMENT", id, request.reason());
        return appointmentResponse(appointment);
    }
    private BookingDtos.SlotResponse slotResponse(AppointmentSlot slot) { return new BookingDtos.SlotResponse(slot.getId(), slot.getStartsAt(), slot.getEndsAt(), slot.getStatus().name(), slot.getDoctor().getId()); }
    private BookingDtos.AppointmentResponse appointmentResponse(Appointment appointment) {
        return new BookingDtos.AppointmentResponse(appointment.getId(), appointment.getSlot().getId(), appointment.getDoctor().getId(), appointment.getDoctor().getUser().getDisplayName(), appointment.getPatient().getId(), appointment.getPatient().getUser().getDisplayName(), appointment.getSlot().getStartsAt(), appointment.getSlot().getEndsAt(), appointment.getStatus(), appointment.getReason(), appointment.getCancellationReason());
    }
}
