// src/main/java/com/example/recruitmenttrainingsystem/dto/TrainingDto.java
package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDto {

    private Long internId;
    private Long candidateId;
    private String fullName;
    private LocalDate startDate;   // ngày bắt đầu TT
    private Long trainingDays;     // số ngày TT

    private String subject1;
    private String subject2;
    private String subject3;
    private String summaryResult;
    private String teamReview;

    // 👉 FE đang đọc t.internStatus, nên field này bắt buộc phải đúng tên
    private String internStatus;   // "Đang thực tập", "Đã kết thúc", ...
}
