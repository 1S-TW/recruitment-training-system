// src/main/java/com/example/recruitmenttrainingsystem/service/CandidateService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.AddCandidateResultDto;
import com.example.recruitmenttrainingsystem.dto.CandidateListDto;
import com.example.recruitmenttrainingsystem.dto.CreateCandidateDto;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.exception.CustomException;
import com.example.recruitmenttrainingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateReviewRepository candidateReviewRepository;
    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final CandidateResultRepository candidateResultRepository;
    private final UserRepository userRepository;

    // ... (Hàm getCandidates, getReviewsByCandidate, createCandidate giữ nguyên) ...
    @Transactional(readOnly = true)
    public List<CandidateListDto> getCandidates(Long planId) {
        // ... (code cũ)
        List<Candidate> candidates =
                (planId != null)
                        ? candidateRepository.findByRecruitmentPlan_RecruitmentPlanId(planId)
                        : candidateRepository.findAll();

        return candidates.stream()
                .map(this::toListDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CandidateReview> getReviewsByCandidate(Long candidateId) {
        // ... (code cũ)
        if (candidateId == null) return Collections.emptyList();
        return candidateReviewRepository.findByCandidate_CandidateId(candidateId);
    }

    @Transactional
    public CandidateListDto createCandidate(CreateCandidateDto dto) {
        // ... (code cũ)
        RecruitmentPlan plan = recruitmentPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch tuyển dụng ID: " + dto.getPlanId()));
        boolean exists = candidateRepository.existsByEmailAndRecruitmentPlan_RecruitmentPlanId(dto.getEmail(), dto.getPlanId());
        if (exists) {
            throw new RuntimeException("Email ứng viên đã tồn tại trong kế hoạch này.");
        }
        Candidate candidate = new Candidate();
        candidate.setFullName(dto.getFullName());
        candidate.setEmail(dto.getEmail());
        candidate.setPhoneNumber(dto.getPhoneNumber());
        candidate.setCvLink(dto.getCvLink());
        candidate.setInterviewDate(dto.getInterviewDate());
        candidate.setRecruitmentPlan(plan);
        Candidate savedCandidate = candidateRepository.save(candidate);
        // Map sang DTO (đã bao gồm cvLink và interviewDate)
        return toListDto(savedCandidate);
    }


    // ✅ ================== THAY THẾ HÀM CŨ ==================
    /**
     * Tạo MỚI hoặc CẬP NHẬT kết quả phỏng vấn và review cho ứng viên
     */
    @Transactional
    public CandidateListDto saveCandidateResult(Long candidateId, AddCandidateResultDto dto, String reviewerEmail) {

        // 1. Tìm ứng viên (Dùng hàm fetch EAGER)
        Candidate candidate = candidateRepository.findByIdWithPlanAndRequestDetails(candidateId)
                .orElseThrow(() -> new CustomException("Không tìm thấy ứng viên: " + candidateId));

        // 2. Tìm người chấm điểm
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new CustomException("Không tìm thấy user: " + reviewerEmail));

        // 3. Tìm Result & Review cũ (nếu có)
        // Chúng ta giả định 1 ứng viên chỉ có 1 kết quả/review cuối cùng
        CandidateResult result = candidateResultRepository.findFirstByCandidate_CandidateIdOrderByResultIdDesc(candidateId)
                .orElse(new CandidateResult()); // Tạo mới nếu không có

        CandidateReview review = candidateReviewRepository.findFirstByCandidate_CandidateIdOrderByReviewIdDesc(candidateId)
                .orElse(new CandidateReview()); // Tạo mới nếu không có

        // Lấy trạng thái PASS cũ (để check quota)
        boolean wasAlreadyPass = result.getFinalResult() != null && result.getFinalResult().equalsIgnoreCase("PASS");
        boolean isFirstTime = result.getResultId() == null; // Đây có phải lần chấm đầu tiên không

        // 4. Logic "Read-only" (Yêu cầu 3 & 4)
        if (!isFirstTime) {
            String oldStatus = review.getCandidateStatus();
            if (oldStatus.equals("Đã nhận việc") || oldStatus.equals("Không nhận việc")) {
                // Nếu trạng thái mới KHÁC trạng thái cũ -> Báo lỗi
                if (!dto.getCandidateStatus().equals(oldStatus)) {
                    throw new CustomException("Không thể cập nhật. Ứng viên đã " + oldStatus + ".");
                }

                // Nếu trạng thái giống, chỉ cho phép cập nhật Note/Lưu ý
                review.setNote(dto.getNote());
                candidateReviewRepository.save(review);

                // Bỏ qua cập nhật Result (vì form FE sẽ bị disable)
                return toListDto(candidate); // Trả về trạng thái cũ
            }
        }

        // 5. Logic Quota (Yêu cầu 1)
        if (dto.getFinalResult().equalsIgnoreCase("PASS")) {
            // Nếu ứng viên này CHƯA PASS và user muốn chấm PASS
            if (!wasAlreadyPass) {
                RecruitmentPlan plan = candidate.getRecruitmentPlan();
                HrRequest request = plan.getRequest();

                // Tính tổng giới hạn "Đầu vào" (soLuong * 2)
                int totalLimit = request.getQuantityCandidates().stream()
                        .mapToInt(qc -> qc.getSoLuong() * 2)
                        .sum();

                // Đếm số ứng viên (DISTINCT) đã PASS
                long currentPassCount = candidateResultRepository
                        .countDistinctPassCandidates(plan.getRecruitmentPlanId());

                if (currentPassCount >= totalLimit) {
                    throw new CustomException("Kế hoạch này đã đạt đủ số lượng 'PASS' (" + currentPassCount + "/" + totalLimit + "). " +
                            "Không thể chấm 'PASS' cho ứng viên này.");
                }
            }
            // Nếu wasAlreadyPass=true, nghĩa là chỉ update, không cần check quota
        }

        // 6. Lưu Review (Trạng Thái)
        review.setCandidate(candidate);
        review.setUser(reviewer);
        review.setCandidateStatus(dto.getCandidateStatus());
        review.setNote(dto.getNote());
        CandidateReview savedReview = candidateReviewRepository.save(review);

        // 7. Lưu Result (Kết quả)
        result.setCandidate(candidate);
        result.setReview(savedReview); // Luôn trỏ đến review mới nhất
        result.setAttendedInterview(dto.getAttendedInterview());
        result.setTestScore(dto.getTestScore());
        result.setInterviewScore(dto.getInterviewScore());
        result.setComment(dto.getComment());
        result.setFinalResult(dto.getFinalResult());
        CandidateResult savedResult = candidateResultRepository.save(result);

        // 8. Cập nhật list (để toListDto chạy đúng)
        // Xóa cái cũ (nếu có) và thêm cái mới
        candidate.getReviews().remove(review);
        candidate.getResults().remove(result);
        candidate.getReviews().add(savedReview);
        candidate.getResults().add(savedResult);

        return toListDto(candidate);
    }
    // ======================================================

    // ... (Hàm toListDto giữ nguyên) ...
    private CandidateListDto toListDto(Candidate c) {

        // Đặt giá trị mặc định
        String status = "Chưa có kết quả";
        BigDecimal testScore = null;
        BigDecimal interviewScore = null;

        // ✅ THÊM BIẾN MẶC ĐỊNH
        String attendedInterview = null;
        String finalResult = null;
        String comment = null;
        String note = null;

        // Lấy review mới nhất (nếu có)
        if (c.getReviews() != null && !c.getReviews().isEmpty()) {
            CandidateReview latestReview = c.getReviews().get(c.getReviews().size() - 1);
            status = latestReview.getCandidateStatus();
            note = latestReview.getNote(); // ✅ LẤY NOTE
        }

        // Lấy kết quả mới nhất (nếu có)
        if (c.getResults() != null && !c.getResults().isEmpty()) {
            CandidateResult latest = c.getResults().get(c.getResults().size() - 1);

            testScore = latest.getTestScore();
            interviewScore = latest.getInterviewScore();

            // ✅ LẤY DỮ LIỆU CÒN THIẾU
            attendedInterview = latest.getAttendedInterview();
            finalResult = latest.getFinalResult();
            comment = latest.getComment();

            // Fallback (Nếu có Result nhưng chưa có Review)
            if (status.equals("Chưa có kết quả") && finalResult != null) {
                String r = finalResult.toUpperCase();
                if (r.equals("PASS")) status = "Đã có kết quả";
                else if (r.equals("FAIL")) status = "Không nhận việc";
                else status = finalResult;
            }
        }

        return CandidateListDto.builder()
                .candidateId(c.getCandidateId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phoneNumber(c.getPhoneNumber())
                .cvLink(c.getCvLink())
                .interviewDate(c.getInterviewDate())
                .testScore(testScore)
                .interviewScore(interviewScore)
                .status(status)
                .attendedInterview(attendedInterview) // ✅ THÊM VÀO BUILDER
                .finalResult(finalResult)           // ✅ THÊM VÀO BUILDER
                .comment(comment)                   // ✅ THÊM VÀO BUILDER
                .note(note)                         // ✅ THÊM VÀO BUILDER
                .recruitmentPlanId(
                        c.getRecruitmentPlan() != null ?
                                c.getRecruitmentPlan().getRecruitmentPlanId() : null
                )
                .recruitmentPlanName(
                        c.getRecruitmentPlan() != null ?
                                c.getRecruitmentPlan().getPlanName() : null
                )
                .build();
    }
}