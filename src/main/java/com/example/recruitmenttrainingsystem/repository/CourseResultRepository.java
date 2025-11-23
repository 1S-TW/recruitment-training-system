package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CourseResult;
import com.example.recruitmenttrainingsystem.dto.CourseScoreDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

// Chỉ cần method có sẵn của JpaRepository + vài query custom
public interface CourseResultRepository extends JpaRepository<CourseResult, Long> {
    List<CourseResult> findByIntern_InternId(Long internId);

    Optional<CourseResult> findByIntern_InternIdAndCourse_CourseName(Long internId, String courseName);
}

