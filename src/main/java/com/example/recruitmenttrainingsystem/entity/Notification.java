// src/main/java/com/example/recruitmenttrainingsystem/entity/Notification.java
package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người thực hiện hành động (có thể null nếu hệ thống bắn)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private User sender;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private User receiver;

    // Loại sự kiện: HR_REQUEST_CREATED, HR_REQUEST_APPROVED, ...
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // Tham chiếu đối tượng (vd: request_id, plan_id)
    @Column(name = "reference_type", length = 50)
    private String referenceType; // "HR_REQUEST", "RECRUITMENT_PLAN"

    @Column(name = "reference_id")
    private Long referenceId;
}
