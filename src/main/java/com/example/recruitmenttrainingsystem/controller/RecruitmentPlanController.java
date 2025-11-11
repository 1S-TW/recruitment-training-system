package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.service.RecruitmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recruitment-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // ✅ Cho phép React truy cập API
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
}
