package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Tên kế hoạch (bắt buộc)
    @Column(nullable = false, name = "plan_name")
    private String planName;

    // ✅ Trạng thái kế hoạch (pending, approved, rejected, v.v.)
    @Column(nullable = false)
    private String status;

    // ✅ Người tạo (optional)
    @Column(name = "created_by")
    private String createdBy;

    // ✅ Ngày tạo mặc định hiện tại
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ Hạn hoàn thành kế hoạch
    @Column(name = "delivery_deadline")
    private LocalDate deliveryDeadline;
}