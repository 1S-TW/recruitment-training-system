// src/main/java/com/example/recruitmenttrainingsystem/repository/NotificationRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Lấy tất cả notification của 1 user, mới nhất trước
    List<Notification> findByReceiver_IdOrderByCreatedAtDesc(UUID receiverId);
}
