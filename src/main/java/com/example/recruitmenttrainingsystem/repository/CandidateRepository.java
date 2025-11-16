// src/main/java/com/example/recruitmenttrainingsystem/repository/CandidateRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // Lọc theo foreign key recruitment_plan_id
    List<Candidate> findByRecruitmentPlan_RecruitmentPlanId(Long recruitmentPlanId);
    // Kiểm tra xem email đã tồn tại trong 1 plan cụ thể chưa
    boolean existsByEmailAndRecruitmentPlan_RecruitmentPlanId(String email, Long planId);
    @Query("SELECT c FROM Candidate c " +
            "LEFT JOIN FETCH c.recruitmentPlan p " +
            "LEFT JOIN FETCH p.request r " +
            "LEFT JOIN FETCH r.quantityCandidates qc " +
            "LEFT JOIN FETCH qc.technology " +
            "WHERE c.candidateId = :candidateId")
    Optional<Candidate> findByIdWithPlanAndRequestDetails(@Param("candidateId") Long candidateId);
}
