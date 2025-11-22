// src/main/java/com/example/recruitmenttrainingsystem/dto/RecruitmentPlanResponse.java
package com.example.recruitmenttrainingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentPlanResponse {

    private Long recruitmentPlanId;
    private String planName;
    private String status;
    private LocalDate recruitmentDeadline;
    private LocalDate deliveryDeadline;
    private LocalDateTime createdAt;
    private String note;

    // ✅ Nhu cầu gốc dùng để tạo kế hoạch
    private SimpleHrRequestDto request;

    // ✅ Tên người từ chối (nếu có)
    private String rejectedByName;

    // ----------------- NESTED DTOS -----------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleHrRequestDto {
        private Long requestId;
        private String requestTitle;
        private SimpleUserDto createdBy; // người lập nhu cầu
        private List<SimpleQuantityCandidateDto> quantityCandidates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleUserDto {
        private String fullName;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleTechnologyDto {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleQuantityCandidateDto {
        private Integer soLuong;
        private SimpleTechnologyDto technology;
    }
}
