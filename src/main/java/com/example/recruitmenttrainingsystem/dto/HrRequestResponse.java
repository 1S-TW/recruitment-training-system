package com.example.recruitmenttrainingsystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HrRequestResponse {

    private Long requestId;
    private String requestTitle;
    private String status;
    private LocalDate expectedDeliveryDate;
    private LocalDateTime createdAt;
    private String note;
    private String createdByName;
    private Integer quantityCandidate;

    // ✅ Thêm danh sách chi tiết công nghệ và số lượng
    private List<Map<String, Object>> techQuantities;

    public HrRequestResponse(
            Long requestId,
            String requestTitle,
            String status,
            LocalDate expectedDeliveryDate,
            LocalDateTime createdAt,
            String note,
            String createdByName,
            Integer quantityCandidate
    ) {
        this.requestId = requestId;
        this.requestTitle = requestTitle;
        this.status = status;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.createdAt = createdAt;
        this.note = note;
        this.createdByName = createdByName;
        this.quantityCandidate = quantityCandidate;
    }

    // Getters
    public Long getRequestId() { return requestId; }
    public String getRequestTitle() { return requestTitle; }
    public String getStatus() { return status; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getNote() { return note; }
    public String getCreatedByName() { return createdByName; }
    public Integer getQuantityCandidate() { return quantityCandidate; }
    public List<Map<String, Object>> getTechQuantities() { return techQuantities; }

    // Setter cho danh sách chi tiết
    public void setTechQuantities(List<Map<String, Object>> techQuantities) {
        this.techQuantities = techQuantities;
    }
}