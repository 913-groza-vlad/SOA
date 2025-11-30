package org.medapp.notificationservice.dto;

import java.time.OffsetDateTime;

public class NotificationDtos {
    public record Response(
            Long id,
            String type,
            String title,
            String message,
            Long appointmentId,
            boolean read,
            OffsetDateTime createdAt
    ) {}
}
