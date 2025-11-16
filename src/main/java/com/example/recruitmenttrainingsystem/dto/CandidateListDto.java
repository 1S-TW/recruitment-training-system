// src/main/java/com/example/recruitmenttrainingsystem/dto/CandidateListDto.java
package com.example.recruitmenttrainingsystem.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateListDto {

    private Long candidateId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String cvLink;
    private LocalDateTime interviewDate;
    private BigDecimal testScore;
    private BigDecimal interviewScore;

    // Trạng thái hiển thị trên cột "Trạng Thái"
    private String status;

    // Tên kế hoạch tuyển dụng
    private Long recruitmentPlanId;
    private String recruitmentPlanName;
}
