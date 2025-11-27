package org.medapp.doctorservice.controller;

import jakarta.validation.Valid;
import org.medapp.doctorservice.domain.Doctor;
import org.medapp.doctorservice.dto.DoctorDtos;
import org.medapp.doctorservice.dto.DoctorMapper;
import org.medapp.doctorservice.service.DoctorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService app;

    public DoctorController(DoctorService app) {
        this.app = app;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody @Valid DoctorDtos.CreateRequest req) {
        Long id = app.create(req);
        return ResponseEntity.ok(id);
    }

    @GetMapping({"", "/"})
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
}
