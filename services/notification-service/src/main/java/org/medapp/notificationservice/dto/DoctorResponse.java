package org.medapp.notificationservice.dto;

public record DoctorResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String specialization,
        String bio,
        boolean active,
        String createdAt,
        Long userId
) {}