// src/main/java/com/example/recruitmenttrainingsystem/repository/RecruitmentPlanRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentPlanRepository extends JpaRepository<RecruitmentPlan, Long> {

    @Query("""
        SELECT DISTINCT r FROM RecruitmentPlan r
        LEFT JOIN FETCH r.request req
        LEFT JOIN FETCH req.createdBy reqCreator
        LEFT JOIN FETCH req.quantityCandidates qc
        LEFT JOIN FETCH qc.technology
        LEFT JOIN FETCH r.createdBy planCreator
        LEFT JOIN FETCH r.confirmedBy planConfirmer
        LEFT JOIN FETCH r.rejectedBy planRejecter
        WHERE (:status IS NULL OR r.status = :status)
        ORDER BY r.createdAt DESC
    """)
    List<RecruitmentPlan> findByStatus(@Param("status") String status);

    // Dùng cho dropdown "kế hoạch đã xác nhận" (không FETCH nặng)
    List<RecruitmentPlan> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    // Kiểm tra/ lấy Plan theo requestId (One-to-One)
    Optional<RecruitmentPlan> findByRequest_RequestId(Long requestId);

    // ⭐ NEW: tìm kế hoạch theo từ khóa trong tên
    List<RecruitmentPlan> findByPlanNameContainingIgnoreCase(String keyword);
}
