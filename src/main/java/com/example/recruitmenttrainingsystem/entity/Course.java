// src/main/java/com/example/recruitmenttrainingsystem/entity/Course.java
package com.example.recruitmenttrainingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_name", nullable = false, unique = true, length = 150)
    private String courseName;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_days")
    private Integer durationDays;

    // 🔴 NEW: map cột display_order trong DB
    @Column(name = "display_order")
    private Integer displayOrder;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    private List<CourseResult> courseResults = new ArrayList<>();
}
