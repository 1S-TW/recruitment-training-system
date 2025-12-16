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

    private SimpleHrRequestDto request;

    // ✅ NEW: người tạo kế hoạch (HR)
    private String createdByName;

    // ✅ NEW: người phê duyệt kế hoạch (QLDT)
    private String confirmedByName;

    // ✅ NEW: thời điểm phê duyệt (nếu bạn cần)
    private LocalDateTime confirmedAt;

    // NEW: tên người từ chối kế hoạch (nếu có)
    private String rejectedByName;

    private Integer totalInput;   // SL đầu vào (gấp đôi nhu cầu)
    private Long passCount;       // số candidate PASS
    private Long internCount;     // số ứng viên đã nhận việc (thực tập sinh)

    // ========== NESTED DTOS ==========

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleHrRequestDto {
        private Long requestId;
        private String requestTitle;
        private SimpleUserDto createdBy;
        private List<SimpleQuantityCandidateDto> quantityCandidates;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleUserDto {
        private String fullName;
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleQuantityCandidateDto {
        private Integer soLuong;
        private SimpleTechnologyDto technology;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleTechnologyDto {
        private Long id;
        private String name;
    }
}
