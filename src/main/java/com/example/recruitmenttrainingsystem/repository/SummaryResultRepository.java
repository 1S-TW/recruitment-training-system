// src/main/java/com/example/recruitmenttrainingsystem/repository/SummaryResultRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.SummaryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryResultRepository extends JpaRepository<SummaryResult, Long> {

    Optional<SummaryResult> findByIntern_InternId(Long internId);

    // Đếm số intern thuộc một kế hoạch, đã hoàn thành & kết quả PASS
    long countByIntern_RecruitmentPlan_RecruitmentPlanIdAndIntern_InternStatusAndInternshipResult(
            Long recruitmentPlanId,
            String internStatus,
            String internshipResult
    );
}
