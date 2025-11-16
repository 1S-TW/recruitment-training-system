// src/main/java/com/example/recruitmenttrainingsystem/repository/CandidateResultRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CandidateResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateResultRepository extends JpaRepository<CandidateResult, Long> {
    long countByCandidate_RecruitmentPlan_RecruitmentPlanIdAndFinalResultIgnoreCase(Long planId, String finalResult);
}
