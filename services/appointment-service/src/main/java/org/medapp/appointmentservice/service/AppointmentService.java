package org.medapp.appointmentservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.appointmentservice.domain.Appointment;
import org.medapp.appointmentservice.dto.AppointmentDtos;
import org.medapp.appointmentservice.dto.AppointmentEvent;
import org.medapp.appointmentservice.repo.AppointmentRepository;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository repo;
    private final RestClient patientRestClient;
    private final RestClient doctorRestClient;
    private final KafkaTemplate<String, String> appointmentKafkaTemplate;
    private final TopicExchange appointmentExchange;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${appointment.kafka.topic:appointment-events}")
    private String appointmentTopic;

    @Value("${appointment.rabbit.routing-key:appointment.created}")
    private String rabbitRoutingKey;

    @Transactional
    public Long create(AppointmentDtos.CreateRequest req) {
        validateDoctorExists(req.doctorId());
        validatePatientExists(req.patientId());
        validateTimeRange(req.startTime(), req.endTime(), req.doctorId(), null);

        Appointment a = Appointment.builder()
                .patientId(req.patientId())
                .doctorId(req.doctorId())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .notes(req.notes())
                .status("SCHEDULED")
                .build();

        repo.save(a);

        publishEvent(a, "CREATED");
        sendNotification(a, "CREATED");

        return a.getId();
    }

    @Transactional(readOnly = true)
    public Appointment get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not_found"));
    }

    @Transactional(readOnly = true)
    public Page<Appointment> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional
    public void update(Long id, AppointmentDtos.UpdateRequest req) {
        Appointment a = get(id);
        validateTimeRange(req.startTime(), req.endTime(), a.getDoctorId(), id);
        a.setStartTime(req.startTime());
        a.setEndTime(req.endTime());
        a.setNotes(req.notes());
        a.setStatus(req.status());
        repo.save(a);

        publishEvent(a, "UPDATED");
        sendNotification(a, "UPDATED");
    }

    @Transactional
    public void cancel(Long id, String reason) {
        Appointment a = get(id);
        a.setStatus("CANCELLED");
        if (reason != null && !reason.isBlank()) {
            String existing = a.getNotes() == null ? "" : a.getNotes() + " ";
            a.setNotes(existing + "[Cancelled: " + reason + "]");
        }
        repo.save(a);

        publishEvent(a, "CANCELLED");
        sendNotification(a, "CANCELLED");
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("invalid_time_range");
        }
    }

    // Validate time range and check overlapping appointments for the given doctor.
    // If excludingAppointmentId is non-null, ignore that appointment (useful for updates).
    private void validateTimeRange(LocalDateTime start, LocalDateTime end, Long doctorId, Long excludingAppointmentId) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("invalid_time_range");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("invalid_time_range");
        }

        if (doctorId == null)
            return;

        // Query repository for overlapping appointments, excluding CANCELLED
        var overlaps = repo.findByDoctorIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                doctorId,
                "CANCELLED",
                end,
                start
        );

        boolean conflict = overlaps.stream()
                .anyMatch(appt -> excludingAppointmentId == null || !appt.getId().equals(excludingAppointmentId));

        if (conflict) {
            throw new IllegalArgumentException("time_conflict");
        }
    }

    private String currentBearerToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }

    private void validatePatientExists(Long patientId) {
        String token = currentBearerToken();
        try {
            var req = patientRestClient.get()
                    .uri("/api/patients/{id}", patientId);
            if (token != null) {
                req = req.header("Authorization", "Bearer " + token);
            }
            req.retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND)
                throw new IllegalArgumentException("invalid_patient");
            throw new IllegalArgumentException("patient_service_error");
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("patient_service_unavailable");
        }
    }

    private void validateDoctorExists(Long doctorId) {
        String token = currentBearerToken();
        try {
            var req = doctorRestClient.get()
                    .uri("/api/doctors/{id}", doctorId);
            if (token != null) {
                req = req.header("Authorization", "Bearer " + token);
            }
            req.retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND)
                throw new IllegalArgumentException("invalid_doctor");
            throw new IllegalArgumentException("doctor_service_error");
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("doctor_service_unavailable");
        }
    }

    private void publishEvent(Appointment a, String type) {
        AppointmentEvent event = AppointmentEvent.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .type(type)
                .build();
        String payload = objectMapper.writeValueAsString(event);
        appointmentKafkaTemplate.send(appointmentTopic, String.valueOf(a.getId()), payload);
    }

    private void sendNotification(Appointment a, String type) {
        Map<String, Object> message = Map.of(
                "type", "APPOINTMENT_" + type,
                "appointmentId", a.getId(),
                "patientId", a.getPatientId(),
                "doctorId", a.getDoctorId(),
                "startTime", a.getStartTime().toString(),
                "endTime", a.getEndTime().toString()
        );
        String payload = objectMapper.writeValueAsString(message);
        rabbitTemplate.convertAndSend(appointmentExchange.getName(), rabbitRoutingKey, payload);
    }
}
