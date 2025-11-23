package com.example.recruitmenttrainingsystem.service;
import com.example.recruitmenttrainingsystem.entity.Course;
import com.example.recruitmenttrainingsystem.entity.CourseResult;
import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.repository.CourseRepository;
import com.example.recruitmenttrainingsystem.repository.CourseResultRepository;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CourseService {


    private final CourseRepository courseRepository;
    private final InternRepository internRepository;
    private final CourseResultRepository courseResultRepository;
    public Course addCourse(Course course) {
        Course saved = courseRepository.save(course);

        // lấy toàn bộ intern đang active
        List<Intern> interns = internRepository.findAll();

        for (Intern intern : interns) {
            CourseResult cr = CourseResult.builder()
                    .course(saved)
                    .intern(intern)
                    .totalScore(BigDecimal.ZERO)
                    .theoryScore(BigDecimal.ZERO)
                    .practiceScore(BigDecimal.ZERO)
                    .attitudeScore(BigDecimal.ZERO)
                    .note("")
                    .build();

            courseResultRepository.save(cr);
        }
        return saved;
    }

}
