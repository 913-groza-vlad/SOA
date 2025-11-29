package org.medapp.appointmentservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AppointmentDtos {
    public record CreateRequest(
            @NotNull Long patientId,
            @NotNull Long doctorId,
            @NotNull @Future LocalDateTime startTime,
            @NotNull @Future LocalDateTime endTime,
            @Size(max=500) String notes
    ) {}

    public record UpdateRequest(
            @NotNull @Future LocalDateTime startTime,
            @NotNull @Future LocalDateTime endTime,
            @Size(max=500) String notes,
            @NotNull String status
    ) {}

    public record Response(
            Long id,
            Long patientId,
            Long doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status,
            String notes,
            String createdAt,
            String updatedAt
    ) {}
}
