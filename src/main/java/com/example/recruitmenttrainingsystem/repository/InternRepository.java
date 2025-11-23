// src/main/java/com/example/recruitmenttrainingsystem/repository/InternRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Intern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternRepository extends JpaRepository<Intern, Long> {

    // Kiểm tra 1 ứng viên đã được tạo Intern chưa
    boolean existsByCandidate_CandidateId(Long candidateId);

    // Lấy danh sách intern theo trạng thái (Đang thực tập, Đã kết thúc...)
    List<Intern> findByInternStatusIgnoreCase(String internStatus);

    // 🔹 THÊM: Đếm số intern của 1 kế hoạch tuyển dụng
    long countByRecruitmentPlan_RecruitmentPlanId(Long recruitmentPlanId);
}
