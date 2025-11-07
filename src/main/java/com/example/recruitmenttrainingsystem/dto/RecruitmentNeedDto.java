package com.example.recruitmenttrainingsystem.dto;

import com.example.recruitmenttrainingsystem.entity.RecruitmentNeedStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RecruitmentNeedDto {
    private Long id;
    private String needName;
    private RecruitmentNeedStatus status;
    private LocalDate handoverDeadline;
    private String createdByUsername; // Tên của PM đã tạo
}