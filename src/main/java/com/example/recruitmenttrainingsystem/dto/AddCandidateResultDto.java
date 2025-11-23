// src/main/java/com/example/recruitmenttrainingsystem/dto/AddCandidateResultDto.java
package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddCandidateResultDto {

    // === Phần Kết quả phỏng vấn ===
    @NotBlank(message = "Vui lòng chọn 'Có' hoặc 'Không' cho việc tham dự phỏng vấn.")
    private String attendedInterview; // "YES" hoặc "NO"

    @Min(value = 0, message = "Điểm test không được nhỏ hơn 0")
    @Max(value = 100, message = "Điểm test không được lớn hơn 100")
    private BigDecimal testScore;

    @Min(value = 0, message = "Điểm phỏng vấn không được nhỏ hơn 0")
    private BigDecimal interviewScore;

    private String comment; // Nhận xét trong "Kết quả phỏng vấn"

    @NotBlank(message = "Kết quả cuối cùng (PASS/FAIL) là bắt buộc.")
    private String finalResult; // "PASS" hoặc "FAIL"

    // === Phần Trạng Thái ===
    @NotBlank(message = "Vui lòng cập nhật trạng thái ứng viên.")
    private String candidateStatus; // "Đã có kết quả", "Không nhận việc", v.v.

    private String note; // Lưu ý trong "Trạng Thái"
}