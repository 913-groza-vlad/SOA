package org.medapp.admineventsservice.repo;

import org.medapp.admineventsservice.domain.AdminEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminEventRepository extends JpaRepository<AdminEvent, Long> {

    Page<AdminEvent> findBySourceOrderByCreatedAtDesc(String source, Pageable pageable);
}
