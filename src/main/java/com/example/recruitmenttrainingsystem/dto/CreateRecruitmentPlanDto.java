package com.example.recruitmenttrainingsystem.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateRecruitmentPlanDto {

    @NotNull(message = "requestId là bắt buộc")
    private Long requestId;

    @NotNull(message = "Tên kế hoạch là bắt buộc")
    private String planName;

    // mặc định "DRAFT" nếu FE không gửi
    private String status;

    @NotNull(message = "Hạn tuyển dụng là bắt buộc")
    private LocalDate recruitmentDeadline;

    @NotNull(message = "Hạn bàn giao là bắt buộc")
    private LocalDate deliveryDeadline;

    private String note;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getRecruitmentDeadline() { return recruitmentDeadline; }
    public void setRecruitmentDeadline(LocalDate recruitmentDeadline) { this.recruitmentDeadline = recruitmentDeadline; }

    public LocalDate getDeliveryDeadline() { return deliveryDeadline; }
    public void setDeliveryDeadline(LocalDate deliveryDeadline) { this.deliveryDeadline = deliveryDeadline; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
