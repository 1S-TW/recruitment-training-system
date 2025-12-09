package com.example.recruitmenttrainingsystem.repository;

import com.example.recruitmenttrainingsystem.entity.CourseResult;
import com.example.recruitmenttrainingsystem.entity.CourseScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseScoreHistoryRepository extends JpaRepository<CourseScoreHistory, Long>{

    // Lấy danh sách lịch sử theo thứ tự lần chấm (1 → 2 → 3)
    List<CourseScoreHistory> findByCourseResult_CourseResultIdOrderByAttemptNumberAsc(Long courseResultId);

    // Đếm số lần đã chấm của 1 môn
    int countByCourseResult(CourseResult courseResult);

    // LẤY LẦN CHẤM CUỐI CÙNG (rất quan trọng để kiểm tra đã ≥7 chưa)
    Optional<CourseScoreHistory> findTopByCourseResultOrderByAttemptNumberDesc(CourseResult courseResult);

    // Hoặc nếu muốn ngắn gọn hơn (dùng trong service)
    default CourseScoreHistory findLatestByCourseResult(CourseResult courseResult) {
        return findTopByCourseResultOrderByAttemptNumberDesc(courseResult)
                .orElse(null);
    }


}

