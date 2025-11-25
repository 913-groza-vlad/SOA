package com.medapp.patientservice.service;

import com.medapp.patientservice.domain.Patient;
import com.medapp.patientservice.dto.PatientDtos;
import com.medapp.patientservice.repo.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository repo;

    @Transactional
    public Long create(PatientDtos.CreateRequest req) {
        if (repo.existsByEmail(req.email())) throw new IllegalArgumentException("email_taken");
        Patient p = Patient.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .phone(req.phone())
                .dateOfBirth(req.dateOfBirth())
                .build();
        repo.save(p);
        return p.getId();
    }

    @Transactional(readOnly = true)
    public Patient get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("not_found"));
    }

    @Transactional(readOnly = true)
    public Page<Patient> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional
    public void update(Long id, PatientDtos.UpdateRequest req) {
        Patient p = get(id);
        // if email is changed, ensure uniqueness
        if (!p.getEmail().equalsIgnoreCase(req.email()) && repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("email_taken");
        }
        p.setFirstName(req.firstName());
        p.setLastName(req.lastName());
        p.setEmail(req.email());
        p.setPhone(req.phone());
        p.setDateOfBirth(req.dateOfBirth());
        repo.save(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("not_found");
        repo.deleteById(id);
    }
}
