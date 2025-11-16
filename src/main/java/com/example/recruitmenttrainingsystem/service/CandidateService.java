// src/main/java/com/example/recruitmenttrainingsystem/service/CandidateService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.CandidateListDto;
import com.example.recruitmenttrainingsystem.entity.Candidate;
import com.example.recruitmenttrainingsystem.entity.CandidateResult;
import com.example.recruitmenttrainingsystem.entity.CandidateReview;
import com.example.recruitmenttrainingsystem.repository.CandidateRepository;
import com.example.recruitmenttrainingsystem.repository.CandidateReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;                    // 👈 THÊM IMPORT NÀY

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateReviewRepository candidateReviewRepository;

    // GET candidates (all hoặc theo planId)
    public List<CandidateListDto> getCandidates(Long planId) {
        List<Candidate> candidates =
                (planId != null)
                        ? candidateRepository.findByRecruitmentPlan_RecruitmentPlanId(planId)
                        : candidateRepository.findAll();

        return candidates.stream()
                .map(this::toListDto)
                .toList();
    }

    // GET review theo ứng viên
    public List<CandidateReview> getReviewsByCandidate(Long candidateId) {
        if (candidateId == null) return Collections.emptyList();
        return candidateReviewRepository.findByCandidate_CandidateId(candidateId);
    }

    // Map ENTITY -> DTO dùng cho FE
    private CandidateListDto toListDto(Candidate c) {

        // status lấy từ CandidateResult cuối cùng
        String status = "Chưa có kết quả";
        BigDecimal testScore = null;
        BigDecimal interviewScore = null;

        if (c.getResults() != null && !c.getResults().isEmpty()) {
            CandidateResult latest = c.getResults().get(c.getResults().size() - 1);

            // map BE → FE status
            if (latest.getFinalResult() != null) {
                String r = latest.getFinalResult().toUpperCase();
                if (r.equals("PASS")) status = "Đã có kết quả";
                if (r.equals("FAIL")) status = "Không nhận việc";
                else status = latest.getFinalResult();
            }

            testScore = latest.getTestScore();
            interviewScore = latest.getInterviewScore();
        }

        return CandidateListDto.builder()
                .candidateId(c.getCandidateId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phoneNumber(c.getPhoneNumber())
                .testScore(testScore)
                .interviewScore(interviewScore)
                .status(status)
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
