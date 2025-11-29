package org.medapp.appointmentservice.repo;

import org.medapp.appointmentservice.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findByDoctorIdAndStartTimeBetween(Long doctorId, LocalDateTime from, LocalDateTime to);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long doctorId,
            String excludedStatus,
            LocalDateTime end,
            LocalDateTime start
    );
}
