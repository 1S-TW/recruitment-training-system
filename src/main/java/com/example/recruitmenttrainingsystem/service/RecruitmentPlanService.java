// src/main/java/com/example/recruitmenttrainingsystem/service/RecruitmentPlanService.java
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
import com.example.recruitmenttrainingsystem.repository.UserRepository;
import com.example.recruitmenttrainingsystem.repository.CandidateResultRepository;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentPlanService {

    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final HrRequestRepository hrRequestRepository;
    private final UserRepository userRepository;

    // 🔹 Dùng để đếm ứng viên PASS / Intern cho từng kế hoạch
    private final CandidateResultRepository candidateResultRepository;
    private final InternRepository internRepository;

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

        // ✅ AI ĐANG ĐĂNG NHẬP LÀ NGƯỜI KHỞI TẠO KẾ HOẠCH
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userRepository.findByEmail(email).orElse(null);
        if (actor != null) {
            plan.setCreatedBy(actor);
        }

        if (plan.getCreatedAt() == null) {
            plan.setCreatedAt(LocalDateTime.now());
        }

        // Lưu kế hoạch trước
        RecruitmentPlan saved = recruitmentPlanRepository.save(plan);

        // ✅ Sau khi tạo kế hoạch: Nhu cầu -> IN_PROGRESS
        if (!"IN_PROGRESS".equalsIgnoreCase(req.getStatus())) {
            req.setStatus("IN_PROGRESS");
            hrRequestRepository.save(req);
        }

        return saved;
    }

    // ✅ PHÊ DUYỆT KẾ HOẠCH
    //    + status kế hoạch: CONFIRMED
    //    + HrRequest.status chỉ để IN_PROGRESS (không được COMPLETED vì còn 2 bước sau)
    @Transactional
    public RecruitmentPlanResponse confirmPlan(Long id) {
        RecruitmentPlan plan = recruitmentPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy kế hoạch tuyển dụng ID: " + id));

        if (!"CONFIRMED".equalsIgnoreCase(plan.getStatus())) {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User actor = userRepository.findByEmail(email).orElse(null);

            plan.setStatus("CONFIRMED");
            if (actor != null) {
                plan.setConfirmedBy(actor);
            }
            if (plan.getConfirmedAt() == null) {
                plan.setConfirmedAt(LocalDateTime.now());
            }

            recruitmentPlanRepository.save(plan);

            HrRequest req = plan.getRequest();
            if (req != null && !"CANCELED".equalsIgnoreCase(req.getStatus())) {
                // Yêu cầu vẫn phải "Đang tiến hành" vì chưa xong các bước quản lý ứng viên + đào tạo
                req.setStatus("IN_PROGRESS");
                hrRequestRepository.save(req);
            }
        }

        return toResponse(plan);
    }

    // ✅ TỪ CHỐI: NEW -> REJECTED + note + update HrRequest
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

        // Chuẩn hoá lý do
        String finalReason = (reason == null || reason.isBlank())
                ? "Không ghi rõ lý do."
                : reason.trim();

        // Lấy user đang đăng nhập – người từ chối
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userRepository.findByEmail(email).orElse(null);

        // --- Cập nhật RecruitmentPlan ---
        plan.setStatus("REJECTED");
        plan.setNote(finalReason);
        if (actor != null) {
            plan.setRejectedBy(actor);
        }
        recruitmentPlanRepository.save(plan);

        // --- Đồng bộ lại HrRequest ---
        HrRequest req = plan.getRequest();
        if (req != null) {
            req.setStatus("CANCELED"); // hiển thị "Bị từ chối" ở FE

            String actorName = (actor != null)
                    ? actor.getFullName()
                    : "Không rõ (hệ thống)";
            String combinedReason =
                    "Người từ chối kế hoạch: " + actorName + ". Lý do: " + finalReason;

            req.setRejectReason(combinedReason);
            hrRequestRepository.save(req);
        }

        return toResponse(plan);
    }

    // ================== PRIVATE MAPPER ==================

    private RecruitmentPlanResponse toResponse(RecruitmentPlan plan) {
        HrRequest req = plan.getRequest();

        // ===== 1. User tạo nhu cầu =====
        RecruitmentPlanResponse.SimpleUserDto userDto = null;
        if (req != null && req.getCreatedBy() != null) {
            User createdBy = req.getCreatedBy();
            userDto = new RecruitmentPlanResponse.SimpleUserDto(
                    createdBy.getFullName(),
                    createdBy.getEmail()
            );
        }

        // ===== 2. Danh sách công nghệ + số lượng trên nhu cầu =====
        List<RecruitmentPlanResponse.SimpleQuantityCandidateDto> qcDtos =
                (req != null && req.getQuantityCandidates() != null
                        ? req.getQuantityCandidates()
                        : List.<QuantityCandidate>of()
                )
                        .stream()
                        .map(this::mapQuantityCandidate)
                        .toList();

        // ===== 3. Đóng gói nhu cầu đơn giản =====
        RecruitmentPlanResponse.SimpleHrRequestDto reqDto = null;
        if (req != null) {
            reqDto = new RecruitmentPlanResponse.SimpleHrRequestDto(
                    req.getRequestId(),
                    req.getRequestTitle(),
                    userDto,
                    qcDtos
            );
        }

        // ===== 4. Người từ chối kế hoạch (nếu có) =====
        String rejectedByName = null;
        if (plan.getRejectedBy() != null) {
            rejectedByName = plan.getRejectedBy().getFullName();
        }

        // ===== 5. TÍNH QUOTA CHO KẾ HOẠCH =====
        int totalOutput = 0; // SL ĐẦU RA (theo nhu cầu)
        if (req != null && req.getQuantityCandidates() != null) {
            totalOutput = req.getQuantityCandidates()
                    .stream()
                    .mapToInt(QuantityCandidate::getSoLuong)
                    .sum();
        }
        int totalInput = totalOutput * 2; // SL ĐẦU VÀO = gấp đôi

        // Số ứng viên PASS (distinct theo candidate)
        long passCount = candidateResultRepository
                .countDistinctPassCandidates(plan.getRecruitmentPlanId());

        // Số thực tập sinh đã được tạo từ kế hoạch này
        long internCount = internRepository
                .countByRecruitmentPlan_RecruitmentPlanId(plan.getRecruitmentPlanId());

        // ===== 6. Trả DTO =====
        return new RecruitmentPlanResponse(
                plan.getRecruitmentPlanId(),
                plan.getPlanName(),
                plan.getStatus(),
                plan.getRecruitmentDeadline(),
                plan.getDeliveryDeadline(),
                plan.getCreatedAt(),
                plan.getNote(),
                reqDto,          // ✅ thông tin nhu cầu
                rejectedByName,  // ✅ người từ chối (nếu có)
                totalInput,      // 🔹 SL đầu vào (dùng cho quota ứng viên / đào tạo)
                passCount,       // 🔹 số candidate PASS
                internCount      // 🔹 số Intern (ứng viên đã nhận việc)
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

    // ================== HÀM MỚI – DROPDOWN ỨNG VIÊN ==================

    @Transactional(readOnly = true)
    public List<PlanOptionDto> getApprovedPlansForDropdown() {
        List<RecruitmentPlan> plans =
                recruitmentPlanRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc("CONFIRMED");

        return plans.stream()
                .map(p -> new PlanOptionDto(p.getRecruitmentPlanId(), p.getPlanName()))
                .toList();
    }

    // ================== HÀM MỚI – LẤY PLAN THEO REQUEST ==================

    @Transactional(readOnly = true)
    public RecruitmentPlanResponse getByRequestId(Long requestId) {
        RecruitmentPlan plan = recruitmentPlanRepository.findByRequest_RequestId(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy kế hoạch cho yêu cầu ID: " + requestId));
        return toResponse(plan);
    }
}
