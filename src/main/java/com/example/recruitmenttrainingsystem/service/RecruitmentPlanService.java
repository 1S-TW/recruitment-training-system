package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.repository.RecruitmentPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentPlanService {

    private final RecruitmentPlanRepository recruitmentPlanRepository;

    /**
     * ✅ Lấy danh sách kế hoạch tuyển dụng (lọc theo trạng thái nếu có)
     */
    public List<RecruitmentPlan> getAllPlans(String status) {
        try {
            return recruitmentPlanRepository.findAllByStatus(status);
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi truy vấn kế hoạch tuyển dụng: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // tránh lỗi 500, trả danh sách rỗng
        }
    }
}
