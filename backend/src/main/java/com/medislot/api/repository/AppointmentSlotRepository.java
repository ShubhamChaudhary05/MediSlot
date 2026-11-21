package com.medislot.api.repository;

import com.medislot.api.domain.AppointmentSlot;
import com.medislot.api.domain.SlotStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AppointmentSlot s join fetch s.doctor where s.id = :id")
    Optional<AppointmentSlot> findForBookingById(@Param("id") UUID id);
    List<AppointmentSlot> findByDoctorIdAndStatusAndStartsAtBetweenOrderByStartsAtAsc(UUID doctorId, SlotStatus status, Instant start, Instant end);
    boolean existsByDoctorIdAndStartsAt(UUID doctorId, Instant startsAt);
}
