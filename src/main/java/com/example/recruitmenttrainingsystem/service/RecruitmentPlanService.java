package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.repository.RecruitmentPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecruitmentPlanService {

    private final RecruitmentPlanRepository recruitmentPlanRepository;

    @Transactional
    public ResponseEntity<?> rejectPlan(Long planId, String rejectionReason) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Kế hoạch không tồn tại với ID: " + planId));

        if (!"PENDING".equals(plan.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ có thể từ chối kế hoạch đang ở trạng thái PENDING."));
        }

        plan.setStatus("CANCELED");

        // ✅ GÁN LÝ DO VÀO TRƯỜNG NOTE
        plan.setNote(rejectionReason);

        // Cần cập nhật trạng thái của HrRequest (nếu cần)
        if (plan.getRequest() != null) {
            plan.getRequest().setStatus("CANCELED");
        }

        recruitmentPlanRepository.save(plan);

        return ResponseEntity.ok(Map.of("message", "Kế hoạch tuyển dụng đã bị từ chối."));
    }
    /**
     * ✅ Lấy toàn bộ danh sách kế hoạch (có thể lọc theo status)
     */
    public List<RecruitmentPlan> getAllPlans(String status) {
        return recruitmentPlanRepository.findByStatus(status);
    }
}
