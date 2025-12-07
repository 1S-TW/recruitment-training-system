package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CourseResult;
import com.example.recruitmenttrainingsystem.entity.CourseScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseScoreHistoryRepository extends JpaRepository<CourseScoreHistory, Long>{

    List<CourseScoreHistory> findByCourseResult_CourseResultIdOrderByAttemptNumberAsc(Long id);
    int countByCourseResult(CourseResult cr);


}

