package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recruitment_request")
@Getter
@Setter
public class RecruitmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requestName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentRequestStatus status;

    // Liên kết với người tạo (PM/HR)
    // Tên này phải khớp với tên trong logic Service
    @ManyToOne
    @JoinColumn(name = "pm_id", nullable = false)
    private User projectManager; // Giả định bạn có entity User

    // ... các trường khác nếu cần
}