package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.RecruitmentPlanResponse;
import com.example.recruitmenttrainingsystem.dto.CreateRecruitmentPlanDto;
import com.example.recruitmenttrainingsystem.dto.PlanOptionDto;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import com.example.recruitmenttrainingsystem.entity.QuantityCandidate;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.entity.User;
import com.example.recruitmenttrainingsystem.repository.HrRequestRepository;
import com.example.recruitmenttrainingsystem.repository.RecruitmentPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentPlanService {

    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final HrRequestRepository hrRequestRepository;

    // ================== PUBLIC APIs ==================

    @Transactional(readOnly = true)
    public List<RecruitmentPlanResponse> getAllPlans(String status) {
        return recruitmentPlanRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RecruitmentPlan createPlan(CreateRecruitmentPlanDto dto) {
        HrRequest req = hrRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy yêu cầu nhân sự ID: " + dto.getRequestId()));

        RecruitmentPlan plan = new RecruitmentPlan();
        plan.setRequest(req);
        plan.setPlanName(dto.getPlanName());

        String status = (dto.getStatus() == null || dto.getStatus().isBlank())
                ? "NEW"
                : dto.getStatus();
        plan.setStatus(status);

        plan.setRecruitmentDeadline(dto.getRecruitmentDeadline());
        plan.setDeliveryDeadline(dto.getDeliveryDeadline());
        plan.setNote(dto.getNote());

        if (plan.getCreatedAt() == null) {
            plan.setCreatedAt(LocalDateTime.now());
        }

        return recruitmentPlanRepository.save(plan);
    }

    // ✅ PHÊ DUYỆT: NEW -> CONFIRMED
    @Transactional
    public RecruitmentPlanResponse confirmPlan(Long id) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy kế hoạch tuyển dụng ID: " + id));

        if ("NEW".equalsIgnoreCase(plan.getStatus())) {
            plan.setStatus("CONFIRMED");
            recruitmentPlanRepository.save(plan);
        }

        return toResponse(plan);
    }

    // ✅ TỪ CHỐI: NEW -> REJECTED + note
    @Transactional
    public RecruitmentPlanResponse rejectPlan(Long id, String reason) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy kế hoạch tuyển dụng ID: " + id));

        if (!"NEW".equalsIgnoreCase(plan.getStatus())) {
            throw new IllegalStateException(
                    "Chỉ được từ chối kế hoạch ở trạng thái 'NEW'. Trạng thái hiện tại: " + plan.getStatus()
            );
        }

        plan.setStatus("REJECTED");
        plan.setNote(reason);
        recruitmentPlanRepository.save(plan);

        return toResponse(plan);
    }

    // ================== PRIVATE MAPPER ==================

    private RecruitmentPlanResponse toResponse(RecruitmentPlan plan) {
        HrRequest req = plan.getRequest();

        // User tạo nhu cầu
        RecruitmentPlanResponse.SimpleUserDto userDto = null;
        User createdBy = req.getCreatedBy();
        if (createdBy != null) {
            userDto = new RecruitmentPlanResponse.SimpleUserDto(
                    createdBy.getFullName(),
                    createdBy.getEmail()
            );
        }

        // Danh sách công nghệ + số lượng
        List<RecruitmentPlanResponse.SimpleQuantityCandidateDto> qcDtos =
                req.getQuantityCandidates().stream()
                        .map(this::mapQuantityCandidate)
                        .toList();

        RecruitmentPlanResponse.SimpleHrRequestDto reqDto =
                new RecruitmentPlanResponse.SimpleHrRequestDto(
                        req.getRequestId(),
                        req.getRequestTitle(),
                        userDto,
                        qcDtos
                );

        return new RecruitmentPlanResponse(
                plan.getRecruitmentPlanId(),
                plan.getPlanName(),
                plan.getStatus(),
                plan.getRecruitmentDeadline(),
                plan.getDeliveryDeadline(),
                plan.getCreatedAt(),
                plan.getNote(),
                reqDto
        );
    }

    private RecruitmentPlanResponse.SimpleQuantityCandidateDto mapQuantityCandidate(QuantityCandidate qc) {
        RecruitmentPlanResponse.SimpleTechnologyDto techDto =
                new RecruitmentPlanResponse.SimpleTechnologyDto(
                        qc.getTechnology().getId(),
                        qc.getTechnology().getName()
                );

        return new RecruitmentPlanResponse.SimpleQuantityCandidateDto(
                qc.getSoLuong(),
                techDto
        );
    }

    // ================== HÀM MỚI – DÙNG CHO DROPDOWN ỨNG VIÊN ==================

    /**
     * Lấy danh sách kế hoạch đã được CONFIRMED để hiển thị ở dropdown
     * "Kế hoạch tuyển dụng" trong màn Quản lý ứng viên.
     * (FE sẽ hiển thị label "Đã xác nhận".)
     */
    @Transactional(readOnly = true)
    public List<PlanOptionDto> getApprovedPlansForDropdown() {
        // Status trong DB là CONFIRMED
        List<RecruitmentPlan> plans =
                recruitmentPlanRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc("CONFIRMED");

        return plans.stream()
                .map(p -> new PlanOptionDto(p.getRecruitmentPlanId(), p.getPlanName()))
                .toList();
    }
}
