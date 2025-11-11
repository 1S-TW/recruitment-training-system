package com.example.recruitmenttrainingsystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HrRequestResponse {
    private Long requestId;
    private String requestTitle;
    private String status;
    private LocalDate expectedDeliveryDate;
    private LocalDateTime createdAt;
    private String note;
    private String createdBy; // chỉ lưu tên người tạo

    // Constructor
    public HrRequestResponse(Long requestId, String requestTitle, String status,
                             LocalDate expectedDeliveryDate, LocalDateTime createdAt,
                             String note, String createdBy) {
        this.requestId = requestId;
        this.requestTitle = requestTitle;
        this.status = status;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.createdAt = createdAt;
        this.note = note;
        this.createdBy = createdBy;
    }

    // Getter & Setter
    public Long getRequestId() { return requestId; }
    public String getRequestTitle() { return requestTitle; }
    public String getStatus() { return status; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getNote() { return note; }
    public String getCreatedBy() { return createdBy; }
}

