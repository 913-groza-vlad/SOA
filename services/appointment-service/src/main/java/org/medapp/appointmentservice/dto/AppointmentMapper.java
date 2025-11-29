package org.medapp.appointmentservice.dto;

import org.medapp.appointmentservice.domain.Appointment;

public class AppointmentMapper {
    public static AppointmentDtos.Response toResponse(Appointment a) {
        return new AppointmentDtos.Response(
                a.getId(),
                a.getPatientId(),
                a.getDoctorId(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getNotes(),
                a.getCreatedAt() != null ? a.getCreatedAt().toString() : null,
                a.getUpdatedAt() != null ? a.getUpdatedAt().toString() : null
        );
    }
}
