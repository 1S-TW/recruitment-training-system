package com.example.recruitmenttrainingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

// thêm import
import java.math.BigDecimal;

@Entity
@Table(name = "course_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_result_id")
    private Long courseResultId;

    // FK -> course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Course course;

    // FK -> intern
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Intern intern;

    @Column(name = "theory_score", precision = 4, scale = 2)
    private BigDecimal theoryScore;

    @Column(name = "practice_score", precision = 4, scale = 2)
    private BigDecimal practiceScore;

    @Column(name = "attitude_score", precision = 4, scale = 2)
    private BigDecimal attitudeScore;

    @Column(name = "total_score", precision = 4, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "note", length = 255)
    private String note;
}
