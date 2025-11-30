package org.medapp.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.notificationservice.domain.Notification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "${notification.rabbit.queue}")
    public void handleAppointmentNotification(String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);
            String type = json.path("type").asText("APPOINTMENT_CREATED");
            long appointmentId = json.path("appointmentId").asLong();
            long patientId = json.path("patientId").asLong();
            long doctorId = json.path("doctorId").asLong();

            // Patient notification
            Notification patientNotification = Notification.builder()
                    .userId(patientId)
                    .type(type)
                    .title("Appointment update")
                    .message("Your appointment #" + appointmentId + " has status " + type)
                    .appointmentId(appointmentId)
                    .read(false)
                    .build();
            notificationService.createNotification(patientNotification);

            // Doctor notification
            Notification doctorNotification = Notification.builder()
                    .userId(doctorId)
                    .type(type)
                    .title("Appointment update")
                    .message("Appointment #" + appointmentId + " with patient " + patientId +
                            " has status " + type)
                    .appointmentId(appointmentId)
                    .read(false)
                    .build();
            notificationService.createNotification(doctorNotification);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

