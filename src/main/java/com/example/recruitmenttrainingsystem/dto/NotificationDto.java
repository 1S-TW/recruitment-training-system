// src/main/java/com/example/recruitmenttrainingsystem/dto/NotificationDto.java
package com.example.recruitmenttrainingsystem.dto;

import com.example.recruitmenttrainingsystem.entity.Notification;
import lombok.Data;

import java.time.Instant;

@Data
public class NotificationDto {

    private Long id;
    private String eventType;
    private String title;
    private String content;
    private boolean read;
    private Instant createdAt;
    private String referenceType;
    private Long referenceId;

    public NotificationDto(Notification n) {
        this.id = n.getId();
        this.eventType = n.getEventType();
        this.title = n.getTitle();
        this.content = n.getContent();
        this.read = n.isRead();
        this.createdAt = n.getCreatedAt();
        this.referenceType = n.getReferenceType();
        this.referenceId = n.getReferenceId();
    }
}
