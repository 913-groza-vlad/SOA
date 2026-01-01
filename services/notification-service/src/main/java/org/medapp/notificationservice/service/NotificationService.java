package org.medapp.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.notificationservice.domain.Notification;
import org.medapp.notificationservice.dto.DoctorResponse;
import org.medapp.notificationservice.dto.NotificationDtos;
import org.medapp.notificationservice.dto.NotificationMapper;
import org.medapp.notificationservice.dto.PatientResponse;
import org.medapp.notificationservice.repo.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repo;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Long> unreadCountTemplate;
    private final JavaMailSender mailSender;
    private final RestClient doctorRestClient;
    private final RestClient patientRestClient;

    private String unreadKey(Long userId) {
        return "notifications:unread:" + userId;
    }

    @Transactional
    public NotificationDtos.Response createNotification(Notification notification) {
        Notification saved = repo.save(notification);

        unreadCountTemplate.opsForValue().increment(unreadKey(saved.getUserId()), 1L);

        // websocket push
        var dto = NotificationMapper.toResponse(saved);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + saved.getUserId(),
                dto
        );

        // email
        sendEmail(saved);

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<NotificationDtos.Response> listForUser(Long userId, Pageable pageable) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationMapper::toResponse);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification n = repo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("not_found"));

        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("forbidden");
        }

        if (!n.isRead()) {
            n.setRead(true);
            repo.save(n);
            unreadCountTemplate.opsForValue().decrement(unreadKey(userId), 1L);
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        Long fromRedis = unreadCountTemplate.opsForValue().get(unreadKey(userId));
        if (fromRedis != null) return fromRedis;

        long count = repo.countByUserIdAndReadFalse(userId);
        unreadCountTemplate.opsForValue().set(unreadKey(userId), count);
        return count;
    }

    private void sendEmail(Notification n) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo("user" + n.getUserId() + "@example.com");
            msg.setSubject(n.getTitle());
            msg.setText(n.getMessage());
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Mail send failed: " + e.getMessage());
        }
    }

    private String currentBearerToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }

    public Long resolveDoctorUserId(Long doctorId) {
        String token = currentBearerToken();
        try {
            var req = doctorRestClient.get()
                    .uri("/api/doctors/{id}", doctorId);
            if (token != null) {
                req = req.header("Authorization", "Bearer " + token);
            }
            DoctorResponse d = req.retrieve().body(DoctorResponse.class);
            System.out.println("Resolved doctor userId: " + d.userId());
            return d.userId();
        } catch (RestClientResponseException ex) {
            System.err.println("Could not resolve doctor userId (HTTP " + ex.getStatusCode() + "): " + ex.getMessage());
            return null;
        } catch (RestClientException ex) {
            System.err.println("Could not resolve doctor userId: " + ex.getMessage());
            return null;
        }
    }

    public Long resolvePatientUserId(Long patientId) {
        String token = currentBearerToken();
        try {
            var req = patientRestClient.get()
                    .uri("/api/patients/{id}", patientId);
            if (token != null) {
                req = req.header("Authorization", "Bearer " + token);
            }
            var p = req.retrieve().body(PatientResponse.class);
            System.out.println("Resolved patient userId: " + p.userId());
            return p.userId();
        } catch (RestClientResponseException ex) {
            System.err.println("Could not resolve patient userId (HTTP " + ex.getStatusCode() + "): " + ex.getMessage());
            return null;
        } catch (RestClientException ex) {
            System.err.println("Could not resolve patient userId: " + ex.getMessage());
            return null;
        }
    }
}
