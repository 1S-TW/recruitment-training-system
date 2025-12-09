// src/main/java/com/example/recruitmenttrainingsystem/entity/SummaryResult.java
package com.example.recruitmenttrainingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "summary_result")
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class SummaryResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "final_summary_id")
    private Long finalSummaryId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intern_id", nullable = false, unique = true)
    @JsonIgnore
    private Intern intern;

    @Column(name = "final_score", precision = 4, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "internship_result", nullable = false, length = 10)
    private String internshipResult = "N/A"; // PASS, FAIL, N/A

    @Column(name = "team_evaluation", length = 100)
    private String teamEvaluation;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate @PrePersist
    void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}