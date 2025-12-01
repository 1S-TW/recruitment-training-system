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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final InternRepository internRepository;
    private final CourseResultRepository courseResultRepository;

    // ===================== 1. LẤY DANH SÁCH =====================
    public List<Course> getAllCourses() {
        // Sắp xếp theo ID tăng dần để list không bị nhảy khi F5
        return courseRepository.findAll(Sort.by(Sort.Direction.ASC, "courseId"));
    }

    // ===================== 2. TẠO MỚI (Admin dùng) =====================
    @Transactional
    public Course createCourse(CourseDto dto) {
        // Validate trùng tên
        if (courseRepository.findByCourseName(dto.getCourseName()).isPresent()) {
            throw new CustomException("Tên môn học đã tồn tại: " + dto.getCourseName());
        }

        Course course = Course.builder()
                .courseName(dto.getCourseName())
                .description(dto.getDescription())
                .durationDays(dto.getDurationDays()) // Lưu số ngày học
                .build();

        // Gọi hàm addCourse bên dưới để khởi tạo dữ liệu cho các intern hiện có
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

        return courseRepository.save(course);
    }

    // ===================== 4. XÓA MÔN HỌC =====================
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CustomException("Môn học không tồn tại"));

        // Kiểm tra an toàn: Chỉ cho xóa nếu chưa có sinh viên nào có điểm
        // (Tức là toàn bộ CourseResult liên quan đều có điểm là null)
        boolean hasData = course.getCourseResults().stream()
                .anyMatch(cr -> cr.getTotalScore() != null
                        || cr.getTheoryScore() != null
                        || cr.getPracticeScore() != null);

        if (hasData) {
            throw new CustomException("Không thể xóa môn học này vì đã có dữ liệu điểm số của thực tập sinh.");
        }

        // Nếu an toàn, xóa các bản ghi CourseResult rỗng trước
        courseResultRepository.deleteAll(course.getCourseResults());
        // Sau đó xóa Course
        courseRepository.delete(course);
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
                    // QUAN TRỌNG: Để null thay vì ZERO để biết là "Chưa học"
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