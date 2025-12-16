// src/main/java/com/example/recruitmenttrainingsystem/entity/Intern.java
package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "intern")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intern_id")
    private Long internId;

    // FK -> recruitment_plan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RecruitmentPlan recruitmentPlan;

    // FK -> candidate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Candidate candidate;

    // ngày bắt đầu thực tập (NOT NULL)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // ngày kết thúc (có thể null nếu đang thực tập)
    @Column(name = "end_date")
    private LocalDate endDate;

    // số ngày thực tập tích lũy
    @Column(name = "internship_days")
    private Integer internshipDays;

    // trạng thái thực tập: "Đang thực tập", "Đã kết thúc", ...
    @Column(name = "intern_status", length = 50, nullable = false)
    private String internStatus;

    // Ghi chú thêm cho thực tập sinh
    @Column(name = "note", length = 255)
    private String note;
}