package org.medapp.admineventsservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.admineventsservice.domain.AdminEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final AdminEventService adminEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "${appointment.rabbit.notification-queue}")
    public void onNotificationMessage(String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);
            String type = json.path("type").asText("NOTIFICATION_EVENT");
            String appointmentId = json.path("appointmentId").isMissingNode()
                    ? null
                    : String.valueOf(json.path("appointmentId").asLong());

            AdminEvent event = AdminEvent.builder()
                    .source("RABBIT_NOTIFICATION")
                    .type(type)
                    .key(appointmentId)
                    .payload(payload)
                    .build();

            adminEventService.saveEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
