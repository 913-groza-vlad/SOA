package org.medapp.notificationservice.controller;

import org.medapp.notificationservice.dto.NotificationDtos;
import org.medapp.notificationservice.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService app;

    public NotificationController(NotificationService app) {
        this.app = app;
    }

    private Long currentUserId(JwtAuthenticationToken auth) {
        Object idClaim = auth.getToken().getClaim("userId");
        if (idClaim instanceof Number n)
            return n.longValue();
        return Long.parseLong(auth.getName());
    }

    @GetMapping({"", "/"})
    public ResponseEntity<Page<NotificationDtos.Response>> list(
            @AuthenticationPrincipal JwtAuthenticationToken auth,
            Pageable pageable) {
        Long userId = currentUserId(auth);
        Page<NotificationDtos.Response> page = app.listForUser(userId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@AuthenticationPrincipal JwtAuthenticationToken auth) {
        Long userId = currentUserId(auth);
        return ResponseEntity.ok(app.getUnreadCount(userId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal JwtAuthenticationToken auth,
            @PathVariable Long id) {
        Long userId = currentUserId(auth);
        app.markAsRead(userId, id);
        return ResponseEntity.noContent().build();
    }
}
