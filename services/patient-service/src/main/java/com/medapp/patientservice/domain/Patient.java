package com.medapp.patientservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String firstName;

    @Column(nullable=false, length=100)
    private String lastName;

    @Column(nullable=false, unique=true, length=200)
    private String email;

    @Column(length=30)
    private String phone;

    private LocalDate dateOfBirth;

    @Column(unique = true)
    private Long userId;

    @Column(nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
