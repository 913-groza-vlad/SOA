package org.medapp.doctorservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.doctorservice.domain.Doctor;
import org.medapp.doctorservice.dto.DoctorDtos;
import org.medapp.doctorservice.repo.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository repo;

    @Transactional
    public Long create(DoctorDtos.CreateRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("email_taken");
        }
        Doctor d = Doctor.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .phone(req.phone())
                .specialization(req.specialization())
                .bio(req.bio())
                .active(req.active() == null ? true : req.active())
                .build();
        repo.save(d);
        return d.getId();
    }

    @Transactional(readOnly = true)
    public Doctor get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not_found"));
    }

    @Transactional(readOnly = true)
    public Page<Doctor> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional
    public void update(Long id, DoctorDtos.UpdateRequest req) {
        Doctor d = get(id);

        if (!d.getEmail().equalsIgnoreCase(req.email()) && repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("email_taken");
        }

        d.setFirstName(req.firstName());
        d.setLastName(req.lastName());
        d.setEmail(req.email());
        d.setPhone(req.phone());
        d.setSpecialization(req.specialization());
        d.setBio(req.bio());
        if (req.active() != null) {
            d.setActive(req.active());
        }

        repo.save(d);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("not_found");
        }
        repo.deleteById(id);
    }
}
