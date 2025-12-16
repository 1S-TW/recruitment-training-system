// src/main/java/com/example/recruitmenttrainingsystem/dto/HrRequestResponse.java
package com.example.recruitmenttrainingsystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HrRequestResponse {

    private Long requestId;
    private String requestTitle;
    private String status;
    private LocalDate expectedDeliveryDate;
    private LocalDateTime createdAt;
    private String note;

    private String createdByName;
    private String approvedByName;       // ✅ người phê duyệt nhu cầu

    private List<TechQuantityDto> techQuantities;

    // ✅ lý do từ chối riêng (đã format: "Người từ chối nhu cầu: ... Lý do: ...")
    private String rejectReason;

    public HrRequestResponse(
            Long requestId,
            String requestTitle,
            String status,
            LocalDate expectedDeliveryDate,
            LocalDateTime createdAt,
            String note,
            String createdByName,
            String approvedByName,
            List<TechQuantityDto> techQuantities,
            String rejectReason
    ) {
        this.requestId = requestId;
        this.requestTitle = requestTitle;
        this.status = status;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.createdAt = createdAt;
        this.note = note;
        this.createdByName = createdByName;
        this.approvedByName = approvedByName;
        this.techQuantities = techQuantities;
        this.rejectReason = rejectReason;
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public List<TechQuantityDto> getTechQuantities() {
        return techQuantities;
    }

    public String getRejectReason() {
        return rejectReason;
    }
}