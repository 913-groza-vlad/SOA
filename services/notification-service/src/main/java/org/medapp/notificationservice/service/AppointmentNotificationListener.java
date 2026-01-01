package org.medapp.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.medapp.notificationservice.domain.Notification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final RestClient patientRestClient;
    private final RestClient doctorRestClient;

    @Value("${notification-mapping.fallback-to-entity-id:false}")
    private boolean fallbackToEntityId;

    @RabbitListener(queues = "${notification.rabbit.queue}")
    public void handleAppointmentNotification(String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);

            String type = json.path("type").asText("APPOINTMENT_EVENT");
            long appointmentId = json.path("appointmentId").asLong();

            Long patientUserId = json.path("patientUserId").isMissingNode() ? null : json.path("patientUserId").asLong();
            Long doctorUserId  = json.path("doctorUserId").isMissingNode() ? null : json.path("doctorUserId").asLong();

            if (patientUserId != null) {
                notificationService.createNotification(Notification.builder()
                        .userId(patientUserId)
                        .type(type)
                        .title("Appointment update")
                        .message("Appointment #" + appointmentId + " status: " + type)
                        .appointmentId(appointmentId)
                        .read(false)
                        .build());
            }

            if (doctorUserId != null) {
                notificationService.createNotification(Notification.builder()
                        .userId(doctorUserId)
                        .type(type)
                        .title("Appointment update")
                        .message("Appointment #" + appointmentId + " status: " + type)
                        .appointmentId(appointmentId)
                        .read(false)
                        .build());
            }

            System.out.println("[notification-service] Stored notifications for appt " + appointmentId);

        } catch (Exception e) {
            System.err.println("[notification-service] Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
