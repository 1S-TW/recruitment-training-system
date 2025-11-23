package com.example.recruitmenttrainingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

// thêm import
import java.math.BigDecimal;

@Entity
@Table(name = "course_result",
        uniqueConstraints = @UniqueConstraint(columnNames = {"intern_id", "course_id"}))
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class CourseResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_result_id")
    private Long courseResultId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intern_id", nullable = false)
    private Intern intern;

    @Column(precision = 4, scale = 2)
    private BigDecimal theoryScore;

    @Column(precision = 4, scale = 2)
    private BigDecimal practiceScore;

    @Column(precision = 4, scale = 2)
    private BigDecimal attitudeScore;

    @Column(precision = 4, scale = 2)
    private BigDecimal totalScore;

    @Column(length = 500)
    private String note;
}