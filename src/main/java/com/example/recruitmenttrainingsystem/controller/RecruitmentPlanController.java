package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.CreateRecruitmentPlanDto;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.service.RecruitmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recruitment-plans")
@RequiredArgsConstructor
public class RecruitmentPlanController {

    private final RecruitmentPlanService recruitmentPlanService;

    // ✅ Lấy danh sách kế hoạch (có thể lọc theo status)
    @GetMapping
    public ResponseEntity<List<RecruitmentPlan>> getAllPlans(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(recruitmentPlanService.getAllPlans(status));
    }

    // ✅ Tạo plan (gắn với requestId)
    @PostMapping
    public ResponseEntity<RecruitmentPlan> createPlan(@Valid @RequestBody CreateRecruitmentPlanDto dto) {
        RecruitmentPlan plan = recruitmentPlanService.createPlan(dto);
        return ResponseEntity.ok(plan);
    }
}