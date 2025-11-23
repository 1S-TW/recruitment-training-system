// src/main/java/com/example/recruitmenttrainingsystem/entity/HrRequest.java
package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    // Ghi chú chung (khi tạo / phê duyệt)
    @Column(name = "note", length = 255)
    private String note;

    // ✅ Lý do từ chối RIÊNG
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "hrRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<QuantityCandidate> quantityCandidates = new ArrayList<>();

    @PrePersist
    void setCreatedAt() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    // GETTER CHO quantityCandidates
    public List<QuantityCandidate> getQuantityCandidates() {
        return quantityCandidates;
    }
}