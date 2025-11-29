package org.medapp.appointmentservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppointmentEvent(
        Long id,
        Long patientId,
        Long doctorId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        String type   // CREATED, UPDATED, CANCELLED
) {
}
