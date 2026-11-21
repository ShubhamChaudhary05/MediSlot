package com.medislot.api.repository;

import com.medislot.api.domain.Appointment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<Appointment> findByDoctorIdOrderByCreatedAtDesc(UUID doctorId);
}
