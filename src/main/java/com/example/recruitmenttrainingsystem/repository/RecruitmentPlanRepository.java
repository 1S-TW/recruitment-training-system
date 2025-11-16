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

    /**
     * ✅ Lấy danh sách kế hoạch theo trạng thái (nếu null thì lấy tất cả)
     */
    @Query("""
        SELECT r FROM RecruitmentPlan r
        WHERE (:status IS NULL OR :status = '' OR r.status = :status)
        ORDER BY r.createdAt DESC
    """)
    List<RecruitmentPlan> findAllByStatus(@Param("status") String status);

    // Kiểm tra/ lấy Plan theo requestId (One-to-One)
    Optional<RecruitmentPlan> findByRequest_RequestId(Long requestId);
}