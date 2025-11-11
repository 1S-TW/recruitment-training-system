package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// (+)
import com.fasterxml.jackson.annotation.JsonIgnore;      // (+)
import lombok.EqualsAndHashCode;                        // (+)
import lombok.ToString;                                 // (+)

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
    @JoinColumn(name = "request_id", referencedColumnName = "request_id", nullable = false, unique = true)
    @ToString.Exclude                   // (+)
    @EqualsAndHashCode.Exclude          // (+)
    @JsonIgnore                         // (+) tránh serialize vòng 1–1 quay lại HrRequest
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
}
