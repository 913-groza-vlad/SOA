package org.medapp.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.medapp.notificationservice.domain.Notification;
import org.medapp.notificationservice.dto.NotificationDtos;
import org.medapp.notificationservice.dto.NotificationMapper;
import org.medapp.notificationservice.repo.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repo;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Long> unreadCountTemplate;
    private final JavaMailSender mailSender;

    private String unreadKey(Long userId) {
        return "notifications:unread:" + userId;
    }

    @Transactional
    public Notification createNotification(Notification notification) {
        Notification saved = repo.save(notification);
        // update unread count in Redis
        unreadCountTemplate.opsForValue().increment(unreadKey(saved.getUserId()), 1L);
        // push to websocket topic
        var dto = NotificationMapper.toResponse(saved);
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + saved.getUserId(),
                dto
        );
        // send email (simple demo, real email address mapping omitted)
        sendEmail(saved);

        return saved;
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
        if (fromRedis != null) {
            return fromRedis;
        }
        long count = repo.countByUserIdAndReadFalse(userId);
        unreadCountTemplate.opsForValue().set(unreadKey(userId), count);
        return count;
    }

    private void sendEmail(Notification n) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("user" + n.getUserId() + "@example.com");
        msg.setSubject(n.getTitle());
        msg.setText(n.getMessage());
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
