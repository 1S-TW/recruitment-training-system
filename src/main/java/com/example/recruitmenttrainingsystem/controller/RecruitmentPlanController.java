package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.service.RecruitmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
   }

