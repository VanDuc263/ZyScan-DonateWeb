package org.example.donatebackend.service;

import org.example.donatebackend.dto.response.NotificationResponse;
import org.example.donatebackend.entity.NotificationEntity;
import org.example.donatebackend.entity.UserEntity;
import org.example.donatebackend.repository.NotificationRepository;
import org.example.donatebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public NotificationEntity createNotification(
            Long userId,
            NotificationEntity.NotificationType type,
            String title,
            String content,
            String redirectUrl,
            String metadata
    ) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setRedirectUrl(redirectUrl);
        notification.setMetadata(metadata);
        notification.setCreatedAt(new Date());

        NotificationEntity saved = notificationRepository.save(notification);

        if (messagingTemplate != null) {
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                messagingTemplate.convertAndSendToUser(
                        user.getUsername(),
                        "/queue/notifications",
                        toResponse(saved)
                );
            }
        }

        return saved;
    }

    public List<NotificationResponse> getMyNotifications() {
        Long userId = getCurrentUser().getId();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount() {
        Long userId = getCurrentUser().getId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long id) {
        Long userId = getCurrentUser().getId();

        NotificationEntity notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("notification not found"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        Long userId = getCurrentUser().getId();

        List<NotificationEntity> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (NotificationEntity notification : notifications) {
            if (!Boolean.TRUE.equals(notification.getIsRead())) {
                notification.setIsRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());
        response.setType(entity.getType());
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setIsRead(entity.getIsRead());
        response.setRedirectUrl(entity.getRedirectUrl());
        response.setMetadata(entity.getMetadata());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found"));
    }
}