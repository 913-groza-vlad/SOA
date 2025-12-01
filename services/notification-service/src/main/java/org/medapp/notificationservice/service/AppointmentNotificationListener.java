package org.medapp.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.notificationservice.domain.Notification;
import org.medapp.notificationservice.dto.DoctorResponse;
import org.medapp.notificationservice.dto.PatientResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient patientRestClient;
    private final RestClient doctorRestClient;

    @RabbitListener(queues = "${notification.rabbit.queue}")
    public void handleAppointmentNotification(String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);
            String type = json.path("type").asText("APPOINTMENT_CREATED");
            long appointmentId = json.path("appointmentId").asLong();
            long patientId = json.path("patientId").asLong();
            long doctorId = json.path("doctorId").asLong();

            // 1) Resolve userIds from patientId / doctorId
            Long patientUserId = resolvePatientUserId(patientId);
            Long doctorUserId = resolveDoctorUserId(doctorId);

            // 2) Patient notification (if mapped)
            if (patientUserId != null) {
                Notification patientNotification = Notification.builder()
                        .userId(patientUserId)
                        .type(type)
                        .title("Appointment update")
                        .message("Your appointment #" + appointmentId + " has status " + type)
                        .appointmentId(appointmentId)
                        .read(false)
                        .build();
                notificationService.createNotification(patientNotification);
            }

            // 3) Doctor notification (if mapped)
            if (doctorUserId != null) {
                Notification doctorNotification = Notification.builder()
                        .userId(doctorUserId)
                        .type(type)
                        .title("Appointment update")
                        .message("Appointment #" + appointmentId + " with patient " + patientId +
                                " has status " + type)
                        .appointmentId(appointmentId)
                        .read(false)
                        .build();
                notificationService.createNotification(doctorNotification);
            }

        } catch (Exception e) {
            System.err.println("Failed to handle appointment notification: " + e.getMessage());
        }
    }

    private Long resolvePatientUserId(Long patientId) {
        try {
            PatientResponse p = patientRestClient.get()
                    .uri("/api/patients/{id}", patientId)
                    .retrieve()
                    .body(PatientResponse.class);
            return p.userId(); // add field to DTO
        } catch (Exception e) {
            System.err.println("Could not resolve patient userId: " + e.getMessage());
            return null;
        }
    }

    private Long resolveDoctorUserId(Long doctorId) {
        try {
            DoctorResponse d = doctorRestClient.get()
                    .uri("/api/doctors/{id}", doctorId)
                    .retrieve()
                    .body(DoctorResponse.class);
            return d.userId(); // add field to DTO
        } catch (Exception e) {
            System.err.println("Could not resolve doctor userId: " + e.getMessage());
            return null;
        }
    }
}
