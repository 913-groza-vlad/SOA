package org.medapp.admineventsservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.admineventsservice.domain.AdminEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AppointmentEventListener {

    private final AdminEventService adminEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${appointment.kafka.topic}")
    private String appointmentTopic;

    @KafkaListener(topics = "${appointment.kafka.topic:appointment-events}", groupId = "admin-events-service")
    public void onAppointmentEvent(String payload) {
        try {
            JsonNode json = objectMapper.readTree(payload);
            String type = json.path("type").asText("APPOINTMENT_EVENT");
            String appointmentId = String.valueOf(json.path("id").asLong());

            AdminEvent event = AdminEvent.builder()
                    .source("KAFKA_APPOINTMENT")
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
