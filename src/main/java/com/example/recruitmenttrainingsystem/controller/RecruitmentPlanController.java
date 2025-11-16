package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.CreateRecruitmentPlanDto;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.service.RecruitmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectPlan(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        String rejectionReason = payload.get("rejectionReason");
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lý do từ chối không được để trống."));
        }

        return recruitmentPlanService.rejectPlan(id, rejectionReason);
    }


    // ✅ Tạo plan (gắn với requestId)
    @PostMapping
    public ResponseEntity<RecruitmentPlan> createPlan(@Valid @RequestBody CreateRecruitmentPlanDto dto) {
        RecruitmentPlan plan = recruitmentPlanService.createPlan(dto);
        return ResponseEntity.ok(plan);
    }
}