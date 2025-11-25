package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDto {

    private Long internId;
    private Long candidateId;

    // 🔥 NEW: cần thiết để FE lọc
    private Long recruitmentPlanId;
    private String recruitmentPlanName;

    private String fullName;

    private LocalDate startDate;
    private Long trainingDays;

    private List<CourseScoreDto> scores;

    private BigDecimal summaryResult;
    private BigDecimal teamReview;
    private String internshipResult;

    private String internStatus;
}
