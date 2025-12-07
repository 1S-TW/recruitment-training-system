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
    private String fullName;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long trainingDays;

    private List<CourseScoreDto> scores;
    private Long recruitmentPlanId;
    private BigDecimal summaryResult;
    private String teamReview;
    private String internshipResult;

    private String internStatus;

}
