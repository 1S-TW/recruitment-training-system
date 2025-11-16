// src/main/java/com/example/recruitmenttrainingsystem/repository/CandidateReviewRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CandidateReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CandidateReviewRepository extends JpaRepository<CandidateReview, Long> {

    // Lấy danh sách review theo ứng viên
    List<CandidateReview> findByCandidate_CandidateId(Long candidateId);
    Optional<CandidateReview> findFirstByCandidate_CandidateIdOrderByReviewIdDesc(Long candidateId);
}
