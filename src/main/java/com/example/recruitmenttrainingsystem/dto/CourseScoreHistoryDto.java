package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseScoreHistoryDto {
    private Integer attemptNumber;
    private BigDecimal theoryScore;
    private BigDecimal practiceScore;
    private BigDecimal attitudeScore;
    private BigDecimal totalScore;
    private String reason;
}
