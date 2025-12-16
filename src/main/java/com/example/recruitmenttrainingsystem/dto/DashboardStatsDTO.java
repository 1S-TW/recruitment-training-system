package com.example.recruitmenttrainingsystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {

    private long totalEnroll;          // Số TTS nhập học
    private long totalGraduate;        // Số TTS tốt nghiệp
    private long totalFail;            // Số TTS fail
    private String passFailRateStr;       // Tỷ lệ pass/fail
    private long totalQuit;            // TTS nghỉ thực tập
    private Double averageFinalScore;  // Điểm tốt nghiệp trung bình
}
