// src/main/java/com/example/recruitmenttrainingsystem/repository/CandidateRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findByRecruitmentPlan_RecruitmentPlanId(Long recruitmentPlanId);

    boolean existsByEmailAndRecruitmentPlan_RecruitmentPlanId(String email, Long planId);

    @Query("""
        SELECT c FROM Candidate c
        LEFT JOIN FETCH c.recruitmentPlan p
        LEFT JOIN FETCH p.request r
        LEFT JOIN FETCH r.quantityCandidates qc
        LEFT JOIN FETCH qc.technology
        WHERE c.candidateId = :candidateId
        """)
    Optional<Candidate> findByIdWithPlanAndRequestDetails(@Param("candidateId") Long candidateId);

    // ===== CHO MÀN ĐÀO TẠO =====
    @Query("""
        SELECT DISTINCT c
        FROM Candidate c
        LEFT JOIN c.results cr
        LEFT JOIN c.reviews rv
        WHERE LOWER(cr.finalResult) = LOWER(:finalResult)
          AND LOWER(rv.candidateStatus) = LOWER(:status)
        """)
    List<Candidate> findForTraining(@Param("finalResult") String finalResult,
                                    @Param("status") String status);
}
