// src/main/java/com/example/recruitmenttrainingsystem/entity/RecruitmentPlan.java
package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    // ===== Quan hệ 1–N với Candidate =====
    @OneToMany(
            mappedBy = "recruitmentPlan",
            cascade = CascadeType.ALL,   // ✅ VIẾT HOA
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
