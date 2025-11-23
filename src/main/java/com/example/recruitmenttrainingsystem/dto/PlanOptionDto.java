// src/main/java/com/example/recruitmenttrainingsystem/dto/PlanOptionDto.java
package com.example.recruitmenttrainingsystem.dto;

public class PlanOptionDto {

    private Long planId;
    private String planName;

    public PlanOptionDto(Long planId, String planName) {
        this.planId = planId;
        this.planName = planName;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getPlanName() {
        return planName;
    }
}