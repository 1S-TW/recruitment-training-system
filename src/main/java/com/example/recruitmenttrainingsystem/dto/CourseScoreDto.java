package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseScoreDto {
    private String courseName;      // Git / Java / SQL
    private BigDecimal theoryScore;
    private BigDecimal practiceScore;
    private BigDecimal attitudeScore;
    private BigDecimal totalScore;
}
