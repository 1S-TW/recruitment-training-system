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

    // dùng để tạo Intern
    private final InternRepository internRepository;

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

    // ================== LƯU KẾT QUẢ + TRẠNG THÁI ==================
    @Transactional
    public CandidateListDto saveCandidateResult(Long candidateId,
                                                AddCandidateResultDto dto,
                                                String reviewerEmail) {

        // 1. Tìm ứng viên
        Candidate candidate = candidateRepository.findByIdWithPlanAndRequestDetails(candidateId)
                .orElseThrow(() -> new CustomException("Không tìm thấy ứng viên: " + candidateId));

        // 2. Tìm người chấm
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

        String previousStatus = review.getCandidateStatus(); // có thể null

        // 4. Nếu đã Đã nhận việc / Không nhận việc -> chỉ cho sửa Note
        if (!isFirstTimeResult && previousStatus != null) {
            if (previousStatus.equals("Đã nhận việc") || previousStatus.equals("Không nhận việc")) {

                // không cho đổi sang trạng thái khác
                if (!dto.getCandidateStatus().equals(previousStatus)) {
                    throw new CustomException("Không thể cập nhật. Ứng viên đã " + previousStatus + ".");
                }

                review.setNote(dto.getNote());
                candidateReviewRepository.save(review);
                return toListDto(candidate);
            }
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

        // 6. Lưu Review ( trạng thái )
        review.setCandidate(candidate);
        review.setUser(reviewer);
        review.setCandidateStatus(dto.getCandidateStatus());
        review.setNote(dto.getNote());
        review.setReviewDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        CandidateReview savedReview = candidateReviewRepository.save(review);

        // 7. Lưu Result
        result.setCandidate(candidate);
        result.setReview(savedReview);
        result.setAttendedInterview(dto.getAttendedInterview());
        result.setTestScore(dto.getTestScore());
        result.setInterviewScore(dto.getInterviewScore());
        result.setComment(dto.getComment());
        result.setFinalResult(dto.getFinalResult());
        CandidateResult savedResult = candidateResultRepository.save(result);

        // đồng bộ list trong entity
        candidate.getReviews().remove(review);
        candidate.getResults().remove(result);
        candidate.getReviews().add(savedReview);
        candidate.getResults().add(savedResult);

        // 8. Tạo THỰC TẬP SINH khi "Đã nhận việc" + PASS
        String statusNow = dto.getCandidateStatus() == null
                ? ""
                : dto.getCandidateStatus().trim().toLowerCase(Locale.ROOT);
        String statusOld = previousStatus == null
                ? ""
                : previousStatus.trim().toLowerCase(Locale.ROOT);

        boolean isAcceptedNow = statusNow.contains("nhận việc");   // ví dụ: "Đã nhận việc"
        boolean wasAcceptedBefore = statusOld.contains("nhận việc");

        if (isAcceptedNow && !wasAcceptedBefore) {

            // bắt buộc phải PASS
            if (!"PASS".equalsIgnoreCase(dto.getFinalResult())) {
                throw new CustomException("Ứng viên phải PASS thì mới tạo thực tập sinh.");
            }

            boolean alreadyIntern = internRepository.existsByCandidate_CandidateId(candidateId);
            if (!alreadyIntern) {
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

    // ================== MAP RA DTO CHO MÀN ỨNG VIÊN ==================
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
