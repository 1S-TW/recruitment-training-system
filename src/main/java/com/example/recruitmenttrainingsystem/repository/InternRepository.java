package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Intern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InternRepository extends JpaRepository<Intern, Long> {

    // Kiểm tra 1 ứng viên đã được tạo Intern chưa
    boolean existsByCandidate_CandidateId(Long candidateId);

    // Lấy danh sách intern theo trạng thái (Đang thực tập, Đã kết thúc...)
    List<Intern> findByInternStatusIgnoreCase(String internStatus);

    // Đếm số intern của 1 kế hoạch tuyển dụng
    long countByRecruitmentPlan_RecruitmentPlanId(Long recruitmentPlanId);

    // Đếm số intern còn "Đang thực tập"
    long countByRecruitmentPlan_RecruitmentPlanIdAndInternStatusIgnoreCase(
            Long recruitmentPlanId,
            String internStatus
    );

    // ⭐ ĐÚNG — InternStatus (không liên quan PASS/FAIL)
    long countByInternStatusIgnoreCase(String internStatus);

    // ⭐ DÙNG LocalDate — CHUẨN
    long countByStartDateBetween(LocalDate start, LocalDate end);

    long countByEndDateBetween(LocalDate start, LocalDate end);

    // ⭐ Nghỉ thực tập (Đã dừng thực tập)
    long countByInternStatusIgnoreCaseAndEndDateBetween(
            String internStatus,
            LocalDate start,
            LocalDate end
    );

    // Lấy danh sách intern theo kế hoạch
    List<Intern> findByRecruitmentPlan_RecruitmentPlanId(Long recruitmentPlanId);
}
