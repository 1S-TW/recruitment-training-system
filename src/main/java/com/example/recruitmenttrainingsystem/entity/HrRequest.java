package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hr_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "request_title", nullable = false, length = 60)
    private String requestTitle;

    // GIỮ NGUYÊN: status là String, không dùng enum
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    // MỚI: createdAt tự động, nullable = false
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // MỚI: note dài hơn
    @Column(name = "note", length = 255)
    private String note;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    // MỚI: Tự động set createdAt khi insert
    @PrePersist
    void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
}
