package com.example.recruitmenttrainingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "candidate_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    // FK -> candidate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Candidate candidate;

    // FK -> candidate_review (có thể null nếu chưa có review)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private CandidateReview review;

    @Column(name = "attended_interview", nullable = false, length = 10)
    private String attendedInterview; // "YES"/"NO" hoặc "Có"/"Không"

    @Column(name = "test_score", precision = 5, scale = 2)
    private BigDecimal testScore;      // điểm test (%)

    @Column(name = "interview_score", precision = 5, scale = 2)
    private BigDecimal interviewScore; // điểm phỏng vấn

    @Column(name = "comment", length = 255)
    private String comment;

    @Column(name = "final_result", nullable = false, length = 10)
    private String finalResult;        // "PASS"/"FAIL" hoặc code khác của bạn
}
