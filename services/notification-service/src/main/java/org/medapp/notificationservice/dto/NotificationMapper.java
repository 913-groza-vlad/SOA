package org.medapp.notificationservice.dto;

import org.medapp.notificationservice.domain.Notification;

public class NotificationMapper {
    public static NotificationDtos.Response toResponse(Notification n) {
        return new NotificationDtos.Response(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getAppointmentId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
