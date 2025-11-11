
package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequestDto {

    @NotBlank(message = "Lý do từ chối không được để trống")
    private String rejectionReason;
}