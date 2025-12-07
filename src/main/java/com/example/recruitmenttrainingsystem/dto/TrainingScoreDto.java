package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingScoreDto {

    private List<CourseScoreDto> scores;   // danh sách điểm từng môn

    private BigDecimal summaryResult;      // final score
    private String teamReview;         // đánh giá team
    private String internshipResult;       // PASS / FAIL / NA
}
