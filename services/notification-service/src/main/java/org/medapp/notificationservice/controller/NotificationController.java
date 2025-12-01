package org.medapp.notificationservice.controller;

import org.medapp.notificationservice.dto.NotificationDtos;
import org.medapp.notificationservice.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService app;

    public NotificationController(NotificationService app) {
        this.app = app;
    }

    private Long currentUserId(Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (auth instanceof JwtAuthenticationToken jwt) {
            Object idClaim = jwt.getToken().getClaim("userId");
            if (idClaim instanceof Number n)
                return n.longValue();
            try {
                return Long.parseLong(jwt.getName());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id in token");
            }
        }

        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid principal name");
        }
    }

    @GetMapping({ "", "/" })
    public ResponseEntity<Page<NotificationDtos.Response>> list(
            JwtAuthenticationToken auth,
            Pageable pageable) {
        Long userId = currentUserId(auth);
        Page<NotificationDtos.Response> page = app.listForUser(userId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(JwtAuthenticationToken auth) {
        Long userId = currentUserId(auth);
        return ResponseEntity.ok(app.getUnreadCount(userId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            JwtAuthenticationToken auth,
            @PathVariable Long id) {
        Long userId = currentUserId(auth);
        app.markAsRead(userId, id);
        return ResponseEntity.noContent().build();
    }
}
