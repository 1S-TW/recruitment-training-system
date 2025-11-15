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



    // ✅ SỬA QUERY: Thêm LEFT JOIN FETCH cho quantityCandidates và technology

    @Query("""

        SELECT DISTINCT r FROM RecruitmentPlan r

        LEFT JOIN FETCH r.request req

        LEFT JOIN FETCH req.createdBy

        LEFT JOIN FETCH req.quantityCandidates qc

        LEFT JOIN FETCH qc.technology

        WHERE (:status IS NULL OR r.status = :status)

        ORDER BY r.createdAt DESC

    """)

    List<RecruitmentPlan> findByStatus(@Param("status") String status);



    // Kiểm tra/ lấy Plan theo requestId (One-to-One)

    Optional<RecruitmentPlan> findByRequest_RequestId(Long requestId);

}