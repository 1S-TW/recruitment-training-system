package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.repository.HrRequestRepository;
import com.example.recruitmenttrainingsystem.repository.RecruitmentPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.recruitmenttrainingsystem.dto.CreateRecruitmentPlanDto;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // ⬅️ thêm import
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentPlanService {

    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final HrRequestRepository hrRequestRepository;

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

    // Tạo Plan gắn với Request (One-to-One)
    @Transactional
    public RecruitmentPlan createPlan(CreateRecruitmentPlanDto dto) {
        HrRequest req = hrRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + dto.getRequestId()));

        // Ngăn tạo trùng Plan cho 1 Request
        recruitmentPlanRepository.findByRequest_RequestId(dto.getRequestId()).ifPresent(p -> {
            throw new IllegalStateException("Yêu cầu này đã có kế hoạch (planId=" + p.getRecruitmentPlanId() + ")");
        });

        // Validate deadline: không vượt expectedDeliveryDate
        if (dto.getDeliveryDeadline().isAfter(req.getExpectedDeliveryDate())) {
            throw new IllegalArgumentException("Hạn bàn giao của kế hoạch không được vượt quá hạn bàn giao của yêu cầu");
        }

        RecruitmentPlan plan = RecruitmentPlan.builder()
                .request(req)
                .planName(dto.getPlanName())
                .status(dto.getStatus() == null ? "DRAFT" : dto.getStatus())
                .recruitmentDeadline(dto.getRecruitmentDeadline())
                .deliveryDeadline(dto.getDeliveryDeadline())
                .note(dto.getNote())
                .build();

        // ✅ CÁCH B: đảm bảo createdAt không null khi dùng Builder
        if (plan.getCreatedAt() == null) {
            plan.setCreatedAt(LocalDateTime.now());
        }

        return recruitmentPlanRepository.save(plan);
    }

    // (Tuỳ chọn) Lấy plan theo requestId
    public RecruitmentPlan getByRequestId(Long requestId) {
        return recruitmentPlanRepository.findByRequest_RequestId(requestId)
                .orElse(null);
    }
}