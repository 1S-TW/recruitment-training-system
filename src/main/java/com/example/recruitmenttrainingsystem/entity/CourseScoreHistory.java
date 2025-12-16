package com.example.recruitmenttrainingsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_result_id", nullable = false)
    private CourseResult courseResult;

    private Integer attemptNumber; // 1, 2, 3
    private BigDecimal theoryScore;
    private BigDecimal practiceScore;
    private BigDecimal attitudeScore;
    private BigDecimal totalScore;
    private String reason; // lý do chấm
}
