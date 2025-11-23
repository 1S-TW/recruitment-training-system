// src/main/java/com/example/recruitmenttrainingsystem/repository/CandidateReviewRepository.java
package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CandidateReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateReviewRepository extends JpaRepository<CandidateReview, Long> {

    // Lấy danh sách review theo ứng viên
    List<CandidateReview> findByCandidate_CandidateId(Long candidateId);

    // Review mới nhất (đã có sẵn)
    Optional<CandidateReview> findFirstByCandidate_CandidateIdOrderByReviewIdDesc(Long candidateId);

    // 🔹 REVIEW ĐẦU TIÊN có status = ... (theo ngày review_date tăng dần)
    Optional<CandidateReview>
    findFirstByCandidate_CandidateIdAndCandidateStatusIgnoreCaseOrderByReviewDateAsc(
            Long candidateId,
            String candidateStatus
    );
}
