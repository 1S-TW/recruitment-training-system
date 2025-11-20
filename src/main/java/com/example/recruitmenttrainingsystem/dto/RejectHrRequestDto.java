// src/main/java/com/example/recruitmenttrainingsystem/dto/RejectHrRequestDto.java
package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectHrRequestDto {

    @NotBlank(message = "Lý do từ chối không được để trống")
    private String rejectionReason;
}
