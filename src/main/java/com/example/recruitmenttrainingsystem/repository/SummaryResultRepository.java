package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.SummaryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryResultRepository extends JpaRepository<SummaryResult, Long> {
    Optional<SummaryResult> findByIntern_InternId(Long internId);
}
