// src/main/java/com/example/recruitmenttrainingsystem/service/CourseService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.CourseDto;
import com.example.recruitmenttrainingsystem.entity.Course;
import com.example.recruitmenttrainingsystem.entity.CourseResult;
import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.exception.CustomException;
import com.example.recruitmenttrainingsystem.repository.CourseRepository;
import com.example.recruitmenttrainingsystem.repository.CourseResultRepository;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final InternRepository internRepository;
    private final CourseResultRepository courseResultRepository;

    // ===================== 1. LẤY DANH SÁCH =====================
    public List<Course> getAllCourses() {
        // 🔁 Sort theo displayOrder trước, rồi courseId để ổn định
        return courseRepository.findAll(
                Sort.by(Sort.Direction.ASC, "displayOrder", "courseId")
        );
    }

    // ===================== 2. TẠO MỚI (Admin dùng) =====================
    @Transactional
    public Course createCourse(CourseDto dto) {
        // Validate trùng tên
        courseRepository.findByCourseName(dto.getCourseName())
                .ifPresent(c -> {
                    throw new CustomException("Tên môn học đã tồn tại: " + dto.getCourseName());
                });

        // đặt thứ tự mặc định = số lượng môn hiện có + 1
        int nextOrder = (int) (courseRepository.count() + 1);

        Course course = Course.builder()
                .courseName(dto.getCourseName())
                .description(dto.getDescription())
                .durationDays(dto.getDurationDays()) // Lưu số ngày học
                .displayOrder(nextOrder)             // 🔴 NEW
                .build();

        return addCourse(course);
    }

    // ===================== 3. CẬP NHẬT =====================
    @Transactional
    public Course updateCourse(Long id, CourseDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy môn học ID: " + id));

        // Kiểm tra trùng tên (trừ chính nó)
        courseRepository.findByCourseName(dto.getCourseName())
                .ifPresent(existing -> {
                    if (!existing.getCourseId().equals(id)) {
                        throw new CustomException("Tên môn học đã tồn tại: " + dto.getCourseName());
                    }
                });

        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setDurationDays(dto.getDurationDays());
        // displayOrder giữ nguyên, không đụng

        return courseRepository.save(course);
    }

    // ===================== 4. XÓA MÔN HỌC =====================
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CustomException("Môn học không tồn tại"));

        boolean hasData = course.getCourseResults().stream()
                .anyMatch(cr -> cr.getTotalScore() != null
                        || cr.getTheoryScore() != null
                        || cr.getPracticeScore() != null);

        if (hasData) {
            throw new CustomException("Không thể xóa môn học này vì đã có dữ liệu điểm số của thực tập sinh.");
        }

        courseResultRepository.deleteAll(course.getCourseResults());
        courseRepository.delete(course);
    }

    // ===================== 5. SẮP XẾP LẠI THỨ TỰ (DRAG & DROP) =====================
    @Transactional
    public void reorderCourses(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return;

        int order = 1;
        for (Long id : courseIds) {
            Course c = courseRepository.findById(id)
                    .orElseThrow(() -> new CustomException("Không tìm thấy môn học ID: " + id));
            c.setDisplayOrder(order++); // set lại thứ tự theo đúng array FE gửi lên
            // không cần save từng cái – JPA dirty checking sẽ tự flush khi kết thúc transaction
        }
    }

    // ===================== LOGIC CORE: Thêm môn & Init data =====================
    @Transactional
    public Course addCourse(Course course) {
        Course saved = courseRepository.save(course);

        // Lấy toàn bộ intern đang active để tạo bảng điểm trống
        List<Intern> interns = internRepository.findAll();

        for (Intern intern : interns) {
            CourseResult cr = CourseResult.builder()
                    .course(saved)
                    .intern(intern)
                    .totalScore(null)
                    .theoryScore(null)
                    .practiceScore(null)
                    .attitudeScore(null)
                    .note("")
                    .build();

            courseResultRepository.save(cr);
        }
        return saved;
    }
}
