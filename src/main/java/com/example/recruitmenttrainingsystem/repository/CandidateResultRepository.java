// src/main/java/com/example/recruitmenttrainingsystem/repository/CandidateResultRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CandidateResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
public interface CandidateResultRepository extends JpaRepository<CandidateResult, Long> {
    long countByCandidate_RecruitmentPlan_RecruitmentPlanIdAndFinalResultIgnoreCase(Long planId, String finalResult);

    // ✅ 1. THÊM: Tìm kết quả MỚI NHẤT (id lớn nhất) của ứng viên
    Optional<CandidateResult> findFirstByCandidate_CandidateIdOrderByResultIdDesc(Long candidateId);

    // ✅ 2. THÊM: Đếm số ứng viên (DISTINCT) đã PASS trong 1 Plan
    @Query("SELECT COUNT(DISTINCT cr.candidate.candidateId) " +
            "FROM CandidateResult cr " +
            "WHERE cr.candidate.recruitmentPlan.recruitmentPlanId = :planId " +
            "AND cr.finalResult = 'PASS'")
    long countDistinctPassCandidates(@Param("planId") Long planId);

    // ✅ 3. THÊM: Kiểm tra xem ứng viên này đã từng PASS chưa
    boolean existsByCandidate_CandidateIdAndFinalResultIgnoreCase(Long candidateId, String finalResult);
}
