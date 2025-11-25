package com.medapp.patientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PatientDtos {
    public record CreateRequest(
            @NotBlank @Size(max=100) String firstName,
            @NotBlank @Size(max=100) String lastName,
            @NotBlank @Email @Size(max=200) String email,
            @Size(max=30) String phone,
            LocalDate dateOfBirth
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max=100) String firstName,
            @NotBlank @Size(max=100) String lastName,
            @NotBlank @Email @Size(max=200) String email,
            @Size(max=30) String phone,
            LocalDate dateOfBirth
    ) {}

    public record Response(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            LocalDate dateOfBirth,
            String createdAt
    ) {}
}
