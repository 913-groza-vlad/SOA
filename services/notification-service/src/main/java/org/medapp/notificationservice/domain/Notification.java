package org.medapp.notificationservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String type; // APPOINTMENT_CREATED / UPDATED / CANCELLED

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String message;

    private Long appointmentId;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
        if (!read)
            read = false;
    }
}
