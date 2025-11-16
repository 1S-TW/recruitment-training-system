package com.example.recruitmenttrainingsystem.dto;

import java.time.LocalDate;
import java.util.List;

public class PlanDefaultsDto {
    private Long requestId;
    private String suggestedPlanName;
    private String status; // mặc định "DRAFT"
    private LocalDate recruitmentDeadline;
    private LocalDate deliveryDeadline;
    private String note;
    private Integer totalCandidates;
    private List<TechQuantityDetail> techQuantities;

    public static class TechQuantityDetail {
        private Long technologyId;
        private String technologyName;
        private Integer soLuong;

        public Long getTechnologyId() { return technologyId; }
        public void setTechnologyId(Long technologyId) { this.technologyId = technologyId; }

        public String getTechnologyName() { return technologyName; }
        public void setTechnologyName(String technologyName) { this.technologyName = technologyName; }

        public Integer getSoLuong() { return soLuong; }
        public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getSuggestedPlanName() { return suggestedPlanName; }
    public void setSuggestedPlanName(String suggestedPlanName) { this.suggestedPlanName = suggestedPlanName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getRecruitmentDeadline() { return recruitmentDeadline; }
    public void setRecruitmentDeadline(LocalDate recruitmentDeadline) { this.recruitmentDeadline = recruitmentDeadline; }
    public LocalDate getDeliveryDeadline() { return deliveryDeadline; }
    public void setDeliveryDeadline(LocalDate deliveryDeadline) { this.deliveryDeadline = deliveryDeadline; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(Integer totalCandidates) { this.totalCandidates = totalCandidates; }
    public List<TechQuantityDetail> getTechQuantities() { return techQuantities; }
    public void setTechQuantities(List<TechQuantityDetail> techQuantities) { this.techQuantities = techQuantities; }
}