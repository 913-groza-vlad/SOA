package org.medapp.doctorservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String firstName;

    @Column(nullable=false, length=100)
    private String lastName;

    @Column(nullable=false, unique=true, length=200)
    private String email;

    @Column(length=30)
    private String phone;

    @Column(length=100)
    private String specialization;

    @Column(length=500)
    private String bio;

    @Column(unique = true)
    private Long userId;

    private boolean active;


    @Column(nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        // default doctors to active if not set
        if (!active) active = true;
    }
}
