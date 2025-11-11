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

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "note", length = 255)
    private String note;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}
