package com.medapp.patientservice.repo;

import com.medapp.patientservice.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByEmail(String email);
    Optional<Patient> findByEmail(String email);
}
