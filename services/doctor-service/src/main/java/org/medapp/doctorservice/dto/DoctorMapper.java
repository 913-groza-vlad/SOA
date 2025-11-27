package org.medapp.doctorservice.dto;

import org.medapp.doctorservice.domain.Doctor;

public class DoctorMapper {

    public static DoctorDtos.Response toResponse(Doctor d) {
        return new DoctorDtos.Response(
                d.getId(),
                d.getFirstName(),
                d.getLastName(),
                d.getEmail(),
                d.getPhone(),
                d.getSpecialization(),
                d.getBio(),
                d.isActive(),
                d.getCreatedAt() != null ? d.getCreatedAt().toString() : null
        );
    }
}
