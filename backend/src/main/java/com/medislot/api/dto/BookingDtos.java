package com.medislot.api.dto;

import com.medislot.api.domain.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class BookingDtos {
    private BookingDtos() { }
    public record CreateSlotRequest(@NotNull @Future Instant startsAt, @NotNull @Future Instant endsAt) { }
    public record SlotResponse(UUID id, Instant startsAt, Instant endsAt, String status, UUID doctorId) { }
    public record BookAppointmentRequest(@NotNull UUID slotId, @Size(max = 1000) String reason) { }
    public record AppointmentResponse(UUID id, UUID slotId, UUID doctorId, String doctorName, UUID patientId, String patientName, Instant startsAt, Instant endsAt, AppointmentStatus status, String reason, String cancellationReason) { }
    public record ChangeAppointmentStatusRequest(@NotNull AppointmentStatus status, @Size(max = 500) String reason) { }
    public record CancelAppointmentRequest(@NotBlank @Size(max = 500) String reason) { }
}
