package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.HrRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- THÊM DÒNG NÀY
import org.springframework.stereotype.Repository;

@Repository
public interface HrRequestRepository extends JpaRepository<HrRequest, Long>, JpaSpecificationExecutor<HrRequest> { // <-- THÊM VÀO ĐÂY
}