// src/main/java/com/example/recruitmenttrainingsystem/controller/NotificationController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.NotificationDto;
import com.example.recruitmenttrainingsystem.entity.Notification;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.NotificationRepository;
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ⭐ GIỜ LÀ: GET /api/notifications
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getMyNotifications(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + email));

        List<Notification> list =
                notificationRepository.findByReceiver_IdOrderByCreatedAtDesc(user.getId());

        List<NotificationDto> dtos = list.stream()
                .map(NotificationDto::new)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication auth) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy notification ID: " + id));

        // Optional: check owner
        String email = auth.getName();
        if (!n.getReceiver().getEmail().equals(email)) {
            return ResponseEntity.status(403)
                    .body("Không được phép cập nhật thông báo của người khác.");
        }

        n.setRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok("Đã đánh dấu đã đọc.");
    }
}
