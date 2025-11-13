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

    /**
     * ✅ API: Lấy danh sách kế hoạch tuyển dụng (có thể lọc theo trạng thái)
     * Ví dụ: /api/recruitment-plans?status=PENDING
     */
    @GetMapping
    public ResponseEntity<List<RecruitmentPlan>> getAllPlans(
            @RequestParam(required = false) String status) {
        List<RecruitmentPlan> plans = recruitmentPlanService.getAllPlans(status);
        return ResponseEntity.ok(plans);
    }


    // ✅ Tạo plan (gắn với requestId)
    @PostMapping
    public ResponseEntity<RecruitmentPlan> createPlan(@Valid @RequestBody CreateRecruitmentPlanDto dto) {
        RecruitmentPlan plan = recruitmentPlanService.createPlan(dto);
        return ResponseEntity.ok(plan);
    }
}