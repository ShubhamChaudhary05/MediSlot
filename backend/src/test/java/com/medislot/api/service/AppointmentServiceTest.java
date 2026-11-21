package com.medislot.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.medislot.api.domain.*;
import com.medislot.api.dto.BookingDtos;
import com.medislot.api.exception.ApiException;
import com.medislot.api.repository.*;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AppointmentServiceTest {
    @Test
    void refusesAPreviouslyBookedSlotBeforeCreatingAnAppointment() {
        AppointmentSlotRepository slots = mock(AppointmentSlotRepository.class);
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        PatientProfileRepository patients = mock(PatientProfileRepository.class);
        ProviderService providers = mock(ProviderService.class);
        AuditService audit = mock(AuditService.class);
        AppointmentService service = new AppointmentService(slots, appointments, patients, providers, audit);

        AppUser patientUser = new AppUser("patient@example.com", "hash", "Patient", UserRole.PATIENT);
        AppUser doctorUser = new AppUser("doctor@example.com", "hash", "Doctor", UserRole.DOCTOR);
        DoctorProfile doctor = new DoctorProfile(doctorUser, "DOC-123");
        AppointmentSlot slot = new AppointmentSlot(doctor, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(5400));
        slot.setStatus(SlotStatus.BOOKED);
        when(patients.findByUserId(patientUser.getId())).thenReturn(Optional.of(new PatientProfile(patientUser)));
        when(slots.findForBookingById(slot.getId())).thenReturn(Optional.of(slot));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.book(patientUser, new BookingDtos.BookAppointmentRequest(slot.getId(), "Consultation")));

        org.junit.jupiter.api.Assertions.assertEquals("SLOT_UNAVAILABLE", exception.getCode());
        verify(appointments, never()).saveAndFlush(any());
    }
}
