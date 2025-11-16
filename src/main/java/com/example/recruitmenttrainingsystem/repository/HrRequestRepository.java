package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.HrRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- THÊM DÒNG NÀY
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrRequestRepository extends JpaRepository<HrRequest, Long>, JpaSpecificationExecutor<HrRequest> { // <-- THÊM VÀO ĐÂY

    // List cho bảng: chỉ cần sort DESC theo createdAt
    List<HrRequest> findAllByOrderByCreatedAtDesc();

    // Chi tiết: join fetch để có quantityCandidates + technology (tránh N+1)
    @Query("""
           select distinct hr from HrRequest hr
           left join fetch hr.quantityCandidates qc
           left join fetch qc.technology t
           left join fetch hr.createdBy cb
           where hr.requestId = :id
           """)
    Optional<HrRequest> findByIdWithTechs(Long id);
}

