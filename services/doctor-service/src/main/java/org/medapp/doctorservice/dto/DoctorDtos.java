package org.medapp.doctorservice.dto;

import jakarta.validation.constraints.*;

public class DoctorDtos {
    public record CreateRequest(
            @NotBlank @Size(max=100) String firstName,
            @NotBlank @Size(max=100) String lastName,
            @NotBlank @Email @Size(max=200) String email,
            @Size(max=30) String phone,
            @Size(max=100) String specialization,
            @Size(max=500) String bio,
            Boolean active
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max=100) String firstName,
            @NotBlank @Size(max=100) String lastName,
            @NotBlank @Email @Size(max=200) String email,
            @Size(max=30) String phone,
            @Size(max=100) String specialization,
            @Size(max=500) String bio,
            Boolean active
    ) {}

    public record Response(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String specialization,
            String bio,
            boolean active,
            String createdAt
    ) {}
}
