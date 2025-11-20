// src/main/java/com/example/recruitmenttrainingsystem/entity/SummaryResult.java
package com.example.recruitmenttrainingsystem.entity;
import com.example.recruitmenttrainingsystem.entity.Intern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "summary_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "final_summary_id")
    private Long finalSummaryId;

    // FK -> intern (1 intern chỉ có 1 summary)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Intern intern;

    @Column(name = "final_score", precision = 4, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "internship_result", nullable = false, length = 10)
    private String internshipResult;   // VD: "PASS" / "FAIL"

    @Column(name = "team_evaluation", precision = 4, scale = 2)
    private BigDecimal teamEvaluation;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onUpdate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        } else {
            updatedAt = LocalDateTime.now();
        }
    }
}
