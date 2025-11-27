package com.medapp.patientservice.controller;

import com.medapp.patientservice.domain.Patient;
import com.medapp.patientservice.dto.PatientDtos;
import com.medapp.patientservice.dto.PatientMapper;
import com.medapp.patientservice.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService app;

    @Autowired
    public PatientController(PatientService app) {
        this.app = app;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody @Valid PatientDtos.CreateRequest req) {
        Long id = app.create(req);
        return ResponseEntity.ok(id);
    }

    @GetMapping({"", "/"})
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
}
