package org.example.donatebackend.controller;

import org.example.donatebackend.dto.response.NotificationResponse;
import org.example.donatebackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getMyNotifications() {
        return notificationService.getMyNotifications();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount() {
        return Map.of("count", notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public Map<String, String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Map.of("message", "Marked as read");
    }

    @PutMapping("/read-all")
    public Map<String, String> markAllAsRead() {
        notificationService.markAllAsRead();
        return Map.of("message", "Marked all as read");
    }
}