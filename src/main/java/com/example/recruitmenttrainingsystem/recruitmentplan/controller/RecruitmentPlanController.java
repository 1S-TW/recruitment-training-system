package com.example.recruitmenttrainingsystem.recruitmentplan.controller;

import com.example.recruitmenttrainingsystem.recruitmentplan.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.recruitmentplan.service.RecruitmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruitment-plans")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RecruitmentPlanController {

    private final RecruitmentPlanService recruitmentPlanService;

    // ✅ Lấy danh sách kế hoạch tuyển dụng (có thể lọc theo status)
    @GetMapping
    public ResponseEntity<List<RecruitmentPlan>> getAllPlans(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(recruitmentPlanService.getAllPlans(status));
    }

    // ✅ Tạo mới 1 kế hoạch
    @PostMapping
    public ResponseEntity<RecruitmentPlan> createPlan(@RequestBody RecruitmentPlan plan) {
        return ResponseEntity.ok(recruitmentPlanService.save(plan));
    }
}
