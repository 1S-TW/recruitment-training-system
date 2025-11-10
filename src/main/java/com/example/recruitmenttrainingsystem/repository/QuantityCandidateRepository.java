package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.QuantityCandidate;
import com.example.recruitmenttrainingsystem.entity.HrRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuantityCandidateRepository extends JpaRepository<QuantityCandidate, Long> {
    // ✅ Thêm phương thức để lấy danh sách QuantityCandidate theo HrRequest
    List<QuantityCandidate> findByHrRequest(HrRequest hrRequest);
}
