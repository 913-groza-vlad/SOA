package org.medapp.admineventsservice.controller;

import org.medapp.admineventsservice.domain.AdminEvent;
import org.medapp.admineventsservice.service.AdminEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final AdminEventService service;

    public AdminEventController(AdminEventService service) {
        this.service = service;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Page<AdminEvent>> all(Pageable pageable) {
        return ResponseEntity.ok(service.listAll(pageable));
    }

    @GetMapping("/appointments")
    public ResponseEntity<Page<AdminEvent>> appointments(Pageable pageable) {
        return ResponseEntity.ok(service.listBySource("KAFKA_APPOINTMENT", pageable));
    }

    @GetMapping("/notifications")
    public ResponseEntity<Page<AdminEvent>> notifications(Pageable pageable) {
        return ResponseEntity.ok(service.listBySource("RABBIT_NOTIFICATION", pageable));
    }
}
