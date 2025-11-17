// src/main/java/com/example/recruitmenttrainingsystem/dto/TrainingDto.java
package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDto {

    private Long internId;       // ID bản ghi intern
    private Long candidateId;    // ID ứng viên gốc
    private String fullName;

    private LocalDate startDate; // ngày bắt đầu thực tập
    private Long trainingDays;   // số ngày thực tập (tính từ startDate -> hôm nay)

    private String subject1;
    private String subject2;
    private String subject3;
    private String summaryResult;
    private String teamReview;

    // trạng thái thực tập: "Đang thực tập", "Đã kết thúc", ...
    private String internStatus;
}
