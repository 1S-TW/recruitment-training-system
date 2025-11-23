// src/main/java/com/example/recruitmenttrainingsystem/entity/Intern.java
package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "intern", uniqueConstraints = @UniqueConstraint(columnNames = "candidate_id"))
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Intern {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intern_id")
    private Long internId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_plan_id", nullable = false)
    private RecruitmentPlan recruitmentPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "internship_days")
    private Integer internshipDays;

    @Column(name = "intern_status", nullable = false, length = 50)
    private String internStatus = "Đang thực tập";

    @Column(length = 500)
    private String note;

    // Quan hệ 1-1 với SummaryResult
    @OneToOne(mappedBy = "intern", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SummaryResult summaryResult;
}