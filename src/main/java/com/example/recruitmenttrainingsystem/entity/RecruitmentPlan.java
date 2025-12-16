// src/main/java/com/example/recruitmenttrainingsystem/entity/RecruitmentPlan.java
package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruitment_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruitment_plan_id")
    private Long recruitmentPlanId;

    @OneToOne
    @JoinColumn(
            name = "request_id",
            referencedColumnName = "request_id",
            nullable = false,
            unique = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private HrRequest request;

    @Column(name = "plan_name", nullable = false, length = 60)
    private String planName;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "recruitment_deadline", nullable = false)
    private LocalDate recruitmentDeadline;

    @Column(name = "delivery_deadline", nullable = false)
    private LocalDate deliveryDeadline;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "note", length = 255)
    private String note;

    // ===== Người tạo kế hoạch =====
    @ManyToOne
    @JoinColumn(name = "created_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    // ===== Người phê duyệt kế hoạch (CONFIRMED) =====
    @ManyToOne
    @JoinColumn(name = "confirmed_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User confirmedBy;

    @Column(name = "confirmed_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime confirmedAt;

    // ===== Quan hệ 1–N với Candidate =====
    @OneToMany(
            mappedBy = "recruitmentPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Candidate> candidates = new ArrayList<>();

    // ===== Người từ chối kế hoạch (nếu có) =====
    @ManyToOne
    @JoinColumn(name = "rejected_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User rejectedBy;
}