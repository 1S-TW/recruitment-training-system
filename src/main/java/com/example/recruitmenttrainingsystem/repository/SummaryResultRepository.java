package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.SummaryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SummaryResultRepository extends JpaRepository<SummaryResult, Long> {

    Optional<SummaryResult> findByIntern_InternId(Long internId);

    // Đếm PASS/FAIL không phân biệt hoa thường
    long countByInternshipResultIgnoreCase(String internshipResult);

    // Đếm PASS/FAIL trong một khoảng thời gian
    long countByInternshipResultIgnoreCaseAndUpdatedAtBetween(
            String result,
            LocalDateTime start,
            LocalDateTime end
    );

    // Lấy danh sách SummaryResult theo kế hoạch
    List<SummaryResult> findByIntern_RecruitmentPlan_RecruitmentPlanId(Long recruitmentPlanId);

    // ⭐ Điểm trung bình TTS PASS theo khoảng thời gian (CHUẨN)
    @Query("SELECT AVG(s.finalScore) FROM SummaryResult s " +
            "WHERE s.internshipResult = 'Đạt' AND s.updatedAt BETWEEN :start AND :end")
    Double avgFinalScore(LocalDateTime start, LocalDateTime end);


    long countByIntern_RecruitmentPlan_RecruitmentPlanIdAndIntern_InternStatusAndInternshipResult(Long planId, String đãHoànThành, String đạt);

    long countByIntern_RecruitmentPlan_RecruitmentPlanIdAndInternshipResultIn(Long planId, List<String> đạt);
}
