// src/main/java/com/example/recruitmenttrainingsystem/repository/CandidateRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // Lọc theo foreign key recruitment_plan_id
    List<Candidate> findByRecruitmentPlan_RecruitmentPlanId(Long recruitmentPlanId);
    // Kiểm tra xem email đã tồn tại trong 1 plan cụ thể chưa
    boolean existsByEmailAndRecruitmentPlan_RecruitmentPlanId(String email, Long planId);
}
