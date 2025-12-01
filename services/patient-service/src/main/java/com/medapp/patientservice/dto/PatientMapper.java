package com.medapp.patientservice.dto;

import com.medapp.patientservice.domain.Patient;

public class PatientMapper {
    public static PatientDtos.Response toResponse(Patient p) {
        return new PatientDtos.Response(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                p.getPhone(),
                p.getDateOfBirth(),
                p.getCreatedAt() != null ? p.getCreatedAt().toString() : null,
                p.getUserId()
        );
    }
}
