    package com.example.recruitmenttrainingsystem.entity;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "candidate")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class    Candidate {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "candidate_id")
        private Long candidateId;

        // FK -> recruitment_plan
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "recruitment_plan_id", nullable = false)
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @JsonIgnore
        private RecruitmentPlan recruitmentPlan;

        @Column(name = "full_name", nullable = false, length = 150)
        private String fullName;

        @Column(name = "email", nullable = false, length = 150)
        private String email;

        @Column(name = "phone_number", length = 20)
        private String phoneNumber;

        @Column(name = "cv_link", length = 255)
        private String cvLink;

        @Column(name = "interview_date")
        private LocalDateTime interviewDate;

        // ========= NEW: gắn với Review & Result =========

        @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @JsonIgnore
        @Builder.Default
        private List<CandidateReview> reviews = new ArrayList<>();

        @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @JsonIgnore
        @Builder.Default
        private List<CandidateResult> results = new ArrayList<>();
    }
