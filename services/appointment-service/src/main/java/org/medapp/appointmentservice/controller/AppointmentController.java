package org.medapp.appointmentservice.controller;

import jakarta.validation.Valid;
import org.medapp.appointmentservice.domain.Appointment;
import org.medapp.appointmentservice.dto.AppointmentDtos;
import org.medapp.appointmentservice.dto.AppointmentMapper;
import org.medapp.appointmentservice.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService app;

    public AppointmentController(AppointmentService app) {
        this.app = app;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody @Valid AppointmentDtos.CreateRequest req) {
        Long id = app.create(req);
        return ResponseEntity.ok(id);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Page<AppointmentDtos.Response>> list(Pageable pageable) {
        Page<AppointmentDtos.Response> page =
                app.list(pageable).map(AppointmentMapper::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDtos.Response> get(@PathVariable Long id) {
        Appointment a = app.get(id);
        return ResponseEntity.ok(AppointmentMapper.toResponse(a));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDtos.Response>> getByPatientId(
            @PathVariable Long patientId) {
        List<Appointment> appointments = app.listByPatientId(patientId);
        List<AppointmentDtos.Response> response = appointments.stream()
                .map(AppointmentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid AppointmentDtos.UpdateRequest req) {
        app.update(id, req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        app.cancel(id, reason);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArg(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
