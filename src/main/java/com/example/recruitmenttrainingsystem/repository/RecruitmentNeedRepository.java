package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.RecruitmentNeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- Thêm import này
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentNeedRepository extends JpaRepository<RecruitmentNeed, Long>,
        JpaSpecificationExecutor<RecruitmentNeed> { // <-- THÊM VÀO ĐÂY
    // Giờ đây, interface này sẽ có phương thức .findAll(Specification, Pageable)
}