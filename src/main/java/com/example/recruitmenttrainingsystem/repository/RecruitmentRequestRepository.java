package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.RecruitmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentRequestRepository extends JpaRepository<RecruitmentRequest, Long>, JpaSpecificationExecutor<RecruitmentRequest> {

}