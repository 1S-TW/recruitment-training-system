package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.QuantityCandidate;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuantityCandidateRepository extends JpaRepository<QuantityCandidate, Long> {
    List<QuantityCandidate> findByHrRequest(HrRequest hrRequest);

    // tiện dùng khi chỉ có requestId
    List<QuantityCandidate> findByHrRequest_RequestId(Long requestId);
}