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
     * Lấy danh sách kế hoạch (có thể lọc theo status)
     */
    public List<RecruitmentPlan> getAllPlans(String status) {
        return recruitmentPlanRepository.findByStatus(status);
    }

    /**
     * Lưu hoặc cập nhật kế hoạch tuyển dụng
     */
    public RecruitmentPlan save(RecruitmentPlan plan) {
        return recruitmentPlanRepository.save(plan);
    }
}