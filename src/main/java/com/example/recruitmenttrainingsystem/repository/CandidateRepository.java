package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // 1. Check trùng email trong cùng plan, TRỪ ứng viên đang sửa (dùng khi update)
    boolean existsByEmailAndRecruitmentPlan_RecruitmentPlanIdAndCandidateIdNot(String email, Long planId, Long candidateId);

    // 2. Check trùng email khi tạo mới (giữ nguyên)
    boolean existsByEmailAndRecruitmentPlan_RecruitmentPlanId(String email, Long planId);

    // 3. Lấy danh sách theo Plan, sắp xếp mới nhất lên đầu (ID giảm dần)
    List<Candidate> findByRecruitmentPlan_RecruitmentPlanIdOrderByCandidateIdDesc(Long recruitmentPlanId);

    // 4. Lấy tất cả, sắp xếp mới nhất lên đầu
    List<Candidate> findAllByOrderByCandidateIdDesc();

    @Query("""
        SELECT c FROM Candidate c
        LEFT JOIN FETCH c.recruitmentPlan p
        LEFT JOIN FETCH p.request r
        LEFT JOIN FETCH r.quantityCandidates qc
        LEFT JOIN FETCH qc.technology
        WHERE c.candidateId = :candidateId
        """)
    Optional<Candidate> findByIdWithPlanAndRequestDetails(@Param("candidateId") Long candidateId);

    // ... (giữ các hàm query khác nếu có)
    @Query("""
        SELECT DISTINCT c
        FROM Candidate c
        LEFT JOIN c.results cr
        LEFT JOIN c.reviews rv
        WHERE LOWER(cr.finalResult) = LOWER(:finalResult)
          AND LOWER(rv.candidateStatus) = LOWER(:status)
        """)
    List<Candidate> findForTraining(@Param("finalResult") String finalResult,
                                    @Param("status") String status);
}