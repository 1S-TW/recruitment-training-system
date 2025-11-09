package com.example.recruitmenttrainingsystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HrRequestResponse {

    private Long requestId;
    private String requestTitle;
    private String status;                    // String, không phải enum
    private LocalDate expectedDeliveryDate;
    private LocalDateTime createdAt;
    private String note;
    private String createdByName;

    // Constructor đúng thứ tự + kiểu
    public HrRequestResponse(
            Long requestId,
            String requestTitle,
            String status,                    // String
            LocalDate expectedDeliveryDate,
            LocalDateTime createdAt,
            String note,
            String createdByName
    ) {
        this.requestId = requestId;
        this.requestTitle = requestTitle;
        this.status = status;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.createdAt = createdAt;
        this.note = note;
        this.createdByName = createdByName;
    }

    // Getters
    public Long getRequestId() { return requestId; }
    public String getRequestTitle() { return requestTitle; }
    public String getStatus() { return status; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getNote() { return note; }
    public String getCreatedByName() { return createdByName; }
}