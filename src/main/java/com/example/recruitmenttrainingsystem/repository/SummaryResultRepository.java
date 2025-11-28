// src/main/java/com/example/recruitmenttrainingsystem/repository/SummaryResultRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.SummaryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SummaryResultRepository extends JpaRepository<SummaryResult, Long> {

    Optional<SummaryResult> findByIntern_InternId(Long internId);

    long countByIntern_RecruitmentPlan_RecruitmentPlanIdAndIntern_InternStatusAndInternshipResult(
            Long recruitmentPlanId,
            String internStatus,
            String internshipResult
    );

    long countByIntern_RecruitmentPlan_RecruitmentPlanIdAndInternshipResultIn(
            Long recruitmentPlanId,
            List<String> internshipResults
    );

    // ⭐ NEW: Đếm theo kết quả thực tập (PASS / FAIL)
    long countByInternshipResultIgnoreCase(String internshipResult);
}
