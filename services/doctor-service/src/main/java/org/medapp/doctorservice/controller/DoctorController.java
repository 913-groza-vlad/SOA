package org.medapp.doctorservice.controller;

import jakarta.validation.Valid;

import org.medapp.doctorservice.domain.Doctor;
import org.medapp.doctorservice.dto.DoctorDtos;
import org.medapp.doctorservice.dto.DoctorMapper;
import org.medapp.doctorservice.repo.DoctorRepository;
import org.medapp.doctorservice.service.DoctorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService app;
    private final DoctorRepository repo;

    public DoctorController(DoctorService app, DoctorRepository repo) {
        this.app = app;
        this.repo = repo;
    }

    @PostMapping({ "", "/" })
    public ResponseEntity<?> create(@RequestBody @Valid DoctorDtos.CreateRequest req) {
        Long id = app.create(req);
        return ResponseEntity.ok(id);
    }

    @GetMapping({ "", "/" })
    public ResponseEntity<Page<DoctorDtos.Response>> list(Pageable pageable) {
        Page<DoctorDtos.Response> page = app.list(pageable)
                .map(DoctorMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDtos.Response> get(@PathVariable Long id) {
        Doctor d = app.get(id);
        return ResponseEntity.ok(DoctorMapper.toResponse(d));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
            @RequestBody @Valid DoctorDtos.UpdateRequest req) {
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
        Doctor d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not_found"));
        d.setUserId(userId);
        repo.save(d);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<DoctorDtos.Response> me(JwtAuthenticationToken auth) {
        Long userId = currentUserId(auth);
        Doctor d = repo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No doctor linked to this user"));
        return ResponseEntity.ok(DoctorMapper.toResponse(d));
    }

}
