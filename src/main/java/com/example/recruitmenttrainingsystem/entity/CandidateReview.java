// src/main/java/com/example/recruitmenttrainingsystem/entity/CandidateReview.java
package com.example.recruitmenttrainingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "candidate_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    // FK -> candidate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Candidate candidate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "candidate_status", nullable = false, length = 30)
    private String candidateStatus;  // ví dụ: "Đã có kết quả", "Đã nhận việc", ...

    // 🔹 ngày cập nhật trạng thái (đặc biệt dùng cho "Đã nhận việc")
    @Column(name = "review_date")
    private LocalDate reviewDate;
}
