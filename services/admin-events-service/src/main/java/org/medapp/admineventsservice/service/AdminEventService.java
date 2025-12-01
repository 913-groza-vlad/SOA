package org.medapp.admineventsservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.admineventsservice.domain.AdminEvent;
import org.medapp.admineventsservice.repo.AdminEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final AdminEventRepository repo;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AdminEvent saveEvent(AdminEvent event) {
        AdminEvent saved = repo.save(event);

        messagingTemplate.convertAndSend("/topic/admin/events", saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AdminEvent> listAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AdminEvent> listBySource(String source, Pageable pageable) {
        return repo.findBySourceOrderByCreatedAtDesc(source, pageable);
    }
}
