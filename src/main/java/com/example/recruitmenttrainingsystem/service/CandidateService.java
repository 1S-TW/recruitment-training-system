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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateReviewRepository candidateReviewRepository;
    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final CandidateResultRepository candidateResultRepository;
    private final UserRepository userRepository;
    private final InternRepository internRepository;  // dùng để tạo TTS

    // ===================== LIST / CREATE =====================

    @Transactional(readOnly = true)
    public List<CandidateListDto> getCandidates(Long planId) {

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
        if (candidateId == null) return Collections.emptyList();
        return candidateReviewRepository.findByCandidate_CandidateId(candidateId);
    }

    @Transactional
    public CandidateListDto createCandidate(CreateCandidateDto dto) {

        RecruitmentPlan plan = recruitmentPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch tuyển dụng ID: " + dto.getPlanId()));

        boolean exists = candidateRepository
                .existsByEmailAndRecruitmentPlan_RecruitmentPlanId(dto.getEmail(), dto.getPlanId());
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
        return toListDto(savedCandidate);
    }

    // ================== LƯU KẾT QUẢ VÀ TRẠNG THÁI ==================

    @Transactional
    public CandidateListDto saveCandidateResult(Long candidateId,
                                                AddCandidateResultDto dto,
                                                String reviewerEmail) {

        // 1. Lấy ứng viên
        Candidate candidate = candidateRepository.findByIdWithPlanAndRequestDetails(candidateId)
                .orElseThrow(() -> new CustomException("Không tìm thấy ứng viên: " + candidateId));

        // 2. Lấy người chấm
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new CustomException("Không tìm thấy user: " + reviewerEmail));

        // 3. Lấy result & review mới nhất (nếu có)
        CandidateResult result = candidateResultRepository
                .findFirstByCandidate_CandidateIdOrderByResultIdDesc(candidateId)
                .orElse(new CandidateResult());

        CandidateReview review = candidateReviewRepository
                .findFirstByCandidate_CandidateIdOrderByReviewIdDesc(candidateId)
                .orElse(new CandidateReview());

        boolean wasAlreadyPass = result.getFinalResult() != null
                && result.getFinalResult().equalsIgnoreCase("PASS");
        boolean isFirstTimeResult = (result.getResultId() == null);

        // trạng thái cũ
        String previousStatus = review.getCandidateStatus();   // có thể null

        // 4
        if (!isFirstTimeResult && previousStatus != null && previousStatus.equals("Đã nhận việc")) {

            // Nếu cố gắng đổi trạng thái khác ngoài "Đã nhận việc" thì lỗi
            if (!dto.getCandidateStatus().equals(previousStatus)) {
                throw new CustomException("Không thể cập nhật. Ứng viên đã " + previousStatus + ".");
            }

            // Chỉ cho phép sửa note khi đã là "Đã nhận việc"
            review.setNote(dto.getNote());
            candidateReviewRepository.save(review);
            return toListDto(candidate);
        }

        // 5. Giữ nguyên logic quota PASS
        if (dto.getFinalResult().equalsIgnoreCase("PASS")) {
            if (!wasAlreadyPass) {
                RecruitmentPlan plan = candidate.getRecruitmentPlan();
                HrRequest request = plan.getRequest();

                int totalLimit = request.getQuantityCandidates().stream()
                        .mapToInt(qc -> qc.getSoLuong() * 2)
                        .sum();

                long currentPassCount = candidateResultRepository
                        .countDistinctPassCandidates(plan.getRecruitmentPlanId());

                if (currentPassCount >= totalLimit) {
                    throw new CustomException("Kế hoạch này đã đạt đủ số lượng 'PASS' (" +
                            currentPassCount + "/" + totalLimit + "). Không thể chấm 'PASS' cho ứng viên này.");
                }
            }
        }
// ... (Logic Quota PASS giữ nguyên, kết thúc ở khoảng dòng 134)

        // 5.5. ✅ KIỂM TRA LOGIC VÔ LÝ: FAIL không thể là Đã nhận việc
        if ("FAIL".equalsIgnoreCase(dto.getFinalResult())) {
            // Kiểm tra trạng thái mới có phải là "Đã nhận việc" hoặc "Đã xác nhận" (cũng là trạng thái cuối) không
            String statusNow = (dto.getCandidateStatus() == null)
                    ? ""
                    : dto.getCandidateStatus().trim().toLowerCase(Locale.ROOT);

            boolean isAcceptedNow = statusNow.contains("nhận việc") || statusNow.contains("xác nhận");

            if (isAcceptedNow) {
                throw new CustomException("Lỗi logic: Không thể đặt trạng thái 'Đã nhận việc' nếu kết quả cuối cùng là 'FAIL'.");
            }
        }

        // 6. Lưu REVIEW (trạng thái)
        review.setCandidate(candidate);
        review.setUser(reviewer);
        review.setCandidateStatus(dto.getCandidateStatus());
        review.setNote(dto.getNote());
        review.setReviewDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        CandidateReview savedReview = candidateReviewRepository.save(review);

        // 7. Lưu RESULT
        result.setCandidate(candidate);
        result.setReview(savedReview);
        result.setAttendedInterview(dto.getAttendedInterview());
        result.setTestScore(dto.getTestScore());
        result.setInterviewScore(dto.getInterviewScore());
        result.setComment(dto.getComment());
        result.setFinalResult(dto.getFinalResult());
        CandidateResult savedResult = candidateResultRepository.save(result);

        // đồng bộ list
        candidate.getReviews().remove(review);
        candidate.getResults().remove(result);
        candidate.getReviews().add(savedReview);
        candidate.getResults().add(savedResult);

        // 8. TẠO THỰC TẬP SINH nếu lần đầu chuyển sang “Đã nhận việc” / “Đã xác nhận”
        String statusNow = dto.getCandidateStatus() == null
                ? ""
                : dto.getCandidateStatus().trim().toLowerCase(Locale.ROOT);
        String statusOld = previousStatus == null
                ? ""
                : previousStatus.trim().toLowerCase(Locale.ROOT);

        // chấp nhận cả text “đã nhận việc” lẫn “đã xác nhận”
        boolean isAcceptedNow =
                statusNow.contains("nhận việc") || statusNow.contains("xác nhận");
        boolean wasAcceptedBefore =
                statusOld.contains("nhận việc") || statusOld.contains("xác nhận");

        if (isAcceptedNow && !wasAcceptedBefore) {

            // ❌ ĐÃ BỎ check bắt buộc PASS:
            // if (!"PASS".equalsIgnoreCase(dto.getFinalResult())) { ... }

            boolean alreadyIntern = internRepository.existsByCandidate_CandidateId(candidateId);
            if (!alreadyIntern) {
                System.out.println(">>> TẠO INTERN cho candidateId = " + candidateId);

                Intern intern = Intern.builder()
                        .candidate(candidate)
                        .recruitmentPlan(candidate.getRecruitmentPlan())
                        .startDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                        .internStatus("Đang thực tập")
                        .internshipDays(0)
                        .note(null)
                        .build();

                internRepository.save(intern);
            }
        }

        return toListDto(candidate);
    }

    // ================== MAP DTO dùng cho màn Ứng viên ==================

    private CandidateListDto toListDto(Candidate c) {

        String status = "Chưa có kết quả";
        BigDecimal testScore = null;
        BigDecimal interviewScore = null;
        String attendedInterview = null;
        String finalResult = null;
        String comment = null;
        String note = null;

        if (c.getReviews() != null && !c.getReviews().isEmpty()) {
            CandidateReview latestReview = c.getReviews().get(c.getReviews().size() - 1);
            status = latestReview.getCandidateStatus();
            note = latestReview.getNote();
        }

        if (c.getResults() != null && !c.getResults().isEmpty()) {
            CandidateResult latest = c.getResults().get(c.getResults().size() - 1);
            testScore = latest.getTestScore();
            interviewScore = latest.getInterviewScore();
            attendedInterview = latest.getAttendedInterview();
            finalResult = latest.getFinalResult();
            comment = latest.getComment();

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
                .attendedInterview(attendedInterview)
                .finalResult(finalResult)
                .comment(comment)
                .note(note)
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
    