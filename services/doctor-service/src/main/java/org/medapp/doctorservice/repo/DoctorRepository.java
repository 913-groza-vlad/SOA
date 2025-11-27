package org.medapp.doctorservice.repo;

import org.medapp.doctorservice.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    boolean existsByEmail(String email);
    Optional<Doctor> findByEmail(String email);
    List<Doctor> findByActiveTrue();
}
