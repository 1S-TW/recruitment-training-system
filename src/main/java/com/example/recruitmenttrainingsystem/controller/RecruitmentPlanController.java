// src/main/java/com/example/recruitmenttrainingsystem/controller/RecruitmentPlanController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.CreateRecruitmentPlanDto;
import com.example.recruitmenttrainingsystem.dto.PlanOptionDto;
import com.example.recruitmenttrainingsystem.dto.RecruitmentPlanResponse;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.service.RecruitmentPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class RecruitmentPlanController {

    private final RecruitmentPlanService recruitmentPlanService;

    // ✅ Lấy danh sách kế hoạch (DTO, có request + quantityCandidates + technology)
    @GetMapping
    public ResponseEntity<List<RecruitmentPlanResponse>> getAllPlans(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(recruitmentPlanService.getAllPlans(status));
    }

    // ✅ Tạo plan (FE không dùng response chi tiết, nên vẫn trả entity)
    @PostMapping
    public ResponseEntity<RecruitmentPlan> createPlan(
            @Valid @RequestBody CreateRecruitmentPlanDto dto) {
        RecruitmentPlan plan = recruitmentPlanService.createPlan(dto);
        return ResponseEntity.ok(plan);
    }

    // ✅ PHÊ DUYỆT kế hoạch → CONFIRMED
    @PutMapping("/{id}/confirm")
    public ResponseEntity<RecruitmentPlanResponse> confirmPlan(@PathVariable Long id) {
        RecruitmentPlanResponse updated = recruitmentPlanService.confirmPlan(id);
        return ResponseEntity.ok(updated);
    }

    // ✅ TỪ CHỐI kế hoạch → REJECTED + lưu lý do
    @PostMapping("/{id}/reject")
    public ResponseEntity<RecruitmentPlanResponse> rejectPlan(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String reason = body.getOrDefault("rejectionReason", "");
        RecruitmentPlanResponse updated = recruitmentPlanService.rejectPlan(id, reason);
        return ResponseEntity.ok(updated);
    }

    // ✅ MỚI: lấy danh sách kế hoạch đã CONFIRMED cho dropdown "Quản lý ứng viên"
    @GetMapping("/approved")
    public ResponseEntity<List<PlanOptionDto>> getApprovedPlans() {
        return ResponseEntity.ok(recruitmentPlanService.getApprovedPlansForDropdown());
    }

    // ✅ MỚI: lấy kế hoạch theo requestId (dùng cho timeline trong chi tiết nhu cầu)
    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<RecruitmentPlanResponse> getByRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(recruitmentPlanService.getByRequestId(requestId));
    }
}