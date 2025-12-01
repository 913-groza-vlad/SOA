package com.medapp.patientservice.controller;

import com.medapp.patientservice.domain.Patient;
import com.medapp.patientservice.dto.PatientDtos;
import com.medapp.patientservice.dto.PatientMapper;
import com.medapp.patientservice.repo.PatientRepository;
import com.medapp.patientservice.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService app;
    private final PatientRepository repo;

    @Autowired
    public PatientController(PatientService app, PatientRepository repo) {
        this.app = app;
        this.repo = repo;
    }

    @PostMapping({ "", "/" })
    public ResponseEntity<?> create(@RequestBody @Valid PatientDtos.CreateRequest req) {
        Long id = app.create(req);
        return ResponseEntity.ok(id);
    }

    @GetMapping({ "", "/" })
    public ResponseEntity<Page<PatientDtos.Response>> list(Pageable pageable) {
        Page<PatientDtos.Response> page = app.list(pageable).map(PatientMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDtos.Response> get(@PathVariable Long id) {
        Patient p = app.get(id);
        return ResponseEntity.ok(PatientMapper.toResponse(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid PatientDtos.UpdateRequest req) {
        app.update(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        app.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArg(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    private Long currentUserId(JwtAuthenticationToken auth) {
        Object idClaim = auth.getToken().getClaim("userId");
        if (idClaim instanceof Number n)
            return n.longValue();
        return Long.parseLong(auth.getName());
    }

    @PostMapping("/{id}/link-user")
    public ResponseEntity<Void> linkUser(
            @PathVariable Long id,
            JwtAuthenticationToken auth) {
        Long userId = currentUserId(auth);
        Patient p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not_found"));
        p.setUserId(userId);
        repo.save(p);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<PatientDtos.Response> me(JwtAuthenticationToken auth) {
        Long userId = currentUserId(auth);
        Patient p = repo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No patient linked to this user"));
        return ResponseEntity.ok(PatientMapper.toResponse(p));
    }
}
