package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CandidateResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CandidateResultRepository extends JpaRepository<CandidateResult, Long> {

    long countByCandidate_RecruitmentPlan_RecruitmentPlanIdAndFinalResultIgnoreCase(Long planId, String finalResult);

    Optional<CandidateResult> findFirstByCandidate_CandidateIdOrderByResultIdDesc(Long candidateId);

    // ✅ SỬA: Đếm số lượng "Đạt" thay vì "PASS"
    @Query("SELECT COUNT(DISTINCT cr.candidate.candidateId) " +
            "FROM CandidateResult cr " +
            "WHERE cr.candidate.recruitmentPlan.recruitmentPlanId = :planId " +
            "AND cr.finalResult = 'Đạt'")
    long countDistinctPassCandidates(@Param("planId") Long planId);

    boolean existsByCandidate_CandidateIdAndFinalResultIgnoreCase(Long candidateId, String finalResult);
}