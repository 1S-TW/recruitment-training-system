package com.example.recruitmenttrainingsystem.dto;

import com.example.recruitmenttrainingsystem.entity.RequestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO trả về danh sách yêu cầu nhân sự
 */
public class HrRequestResponse {

    private Long requestId;
    private String requestTitle;
    private RequestStatus status;           // SỬA: Dùng enum
    private LocalDate expectedDeliveryDate;
    private LocalDateTime createdAt;
    private String note;
    private String createdByName;           // SỬA: Đổi tên field

    // Constructor – PHẢI ĐÚNG THỨ TỰ + KIỂU
    public HrRequestResponse(
            Long requestId,
            String requestTitle,
            RequestStatus status,           // enum
            LocalDate expectedDeliveryDate,
            LocalDateTime createdAt,
            String note,
            String createdByName            // tên khớp với service
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
    public RequestStatus getStatus() { return status; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getNote() { return note; }
    public String getCreatedByName() { return createdByName; } // tên đúng
}