// src/main/java/com/example/recruitmenttrainingsystem/service/TrainingService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.CourseScoreDto;
import com.example.recruitmenttrainingsystem.dto.TrainingDto;
import com.example.recruitmenttrainingsystem.dto.TrainingScoreDto;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingService {

    private final InternRepository internRepository;
    private final CourseRepository courseRepository;
    private final CourseResultRepository courseResultRepository;
    private final SummaryResultRepository summaryResultRepository;

    // NEW: thêm repository để cập nhật trạng thái kế hoạch & nhu cầu
    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final HrRequestRepository hrRequestRepository;

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    // ==================== GET ALL ====================
    public List<TrainingDto> getAll() {
        return internRepository.findAll().stream()
                .map(this::toTrainingDto)
                .toList();
    }

    // ==================== UPDATE SCORES ====================
    @Transactional
    public TrainingDto updateScores(Long internId, TrainingScoreDto dto) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thực tập sinh ID: " + internId));

        // === Cập nhật điểm từng môn ===
        if (dto.getScores() != null && !dto.getScores().isEmpty()) {
            for (CourseScoreDto s : dto.getScores()) {
                Course course = courseRepository.findByCourseName(s.getCourseName())
                        .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại: " + s.getCourseName()));

                CourseResult cr = courseResultRepository
                        .findByIntern_InternIdAndCourse_CourseName(internId, course.getCourseName())
                        .orElseGet(() -> CourseResult.builder()
                                .intern(intern)
                                .course(course)
                                .build());

                cr.setTheoryScore(s.getTheoryScore());
                cr.setPracticeScore(s.getPracticeScore());
                cr.setAttitudeScore(s.getAttitudeScore());

                if (s.getTheoryScore() != null && s.getPracticeScore() != null && s.getAttitudeScore() != null) {
                    BigDecimal total = s.getTheoryScore()
                            .add(s.getPracticeScore())
                            .add(s.getAttitudeScore())
                            .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
                    cr.setTotalScore(total);
                } else {
                    cr.setTotalScore(null);
                }
                courseResultRepository.save(cr);
            }
        }

        // === Cập nhật SummaryResult (luôn tồn tại) ===
        SummaryResult summary = summaryResultRepository.findByIntern_InternId(internId)
                .orElseGet(() -> SummaryResult.builder()
                        .intern(intern)
                        .internshipResult("NA")   // đảm bảo không null
                        .build());

        summary.setFinalScore(dto.getSummaryResult());
        summary.setTeamEvaluation(dto.getTeamReview());

        String result = dto.getInternshipResult();
        String finalResult = (result == null || result.trim().isEmpty())
                ? "NA"
                : result.trim().toUpperCase();

        if (!Set.of("PASS", "FAIL", "NA").contains(finalResult)) {
            throw new IllegalArgumentException("internshipResult chỉ được là PASS, FAIL hoặc NA");
        }
        summary.setInternshipResult(finalResult);

        summaryResultRepository.save(summary);

        // === Cập nhật trạng thái thực tập của intern ===
        String newStatus;

        if ("Đã dừng thực tập".equals(intern.getInternStatus())) {
            newStatus = "Đã dừng thực tập"; // giữ nguyên nếu đã dừng
        } else {
            // Kiểm tra tất cả môn đã có đủ 3 điểm chưa
            boolean allCompleted = courseResultRepository.findByIntern_InternId(internId).stream()
                    .allMatch(cr -> cr.getTheoryScore() != null
                            && cr.getPracticeScore() != null
                            && cr.getAttitudeScore() != null);

            if (allCompleted && "PASS".equals(summary.getInternshipResult())) {
                newStatus = "Đã hoàn thành";
            } else {
                newStatus = "Đang thực tập";
            }
        }

        intern.setInternStatus(newStatus);
        internRepository.save(intern);

        // Sau khi cập nhật điểm, kiểm tra xem kế hoạch / nhu cầu đã kết thúc (thành công hoặc thất bại) chưa
        checkRequestAndPlanStatusByInternId(internId);

        return toTrainingDto(intern);
    }

    // ==================== CHUYỂN ĐỔI DTO – LUÔN HIỆN ĐỦ MÔN ====================
    public TrainingDto toTrainingDto(Intern intern) {
        LocalDate today = LocalDate.now(ZONE_VN);

        // 1. Lấy tất cả môn học trong hệ thống
        List<Course> allCourses = courseRepository.findAll();

        // 2. Lấy điểm hiện có của intern này
        List<CourseResult> results = courseResultRepository.findByIntern_InternId(intern.getInternId());

        // 3. Tạo danh sách điểm đầy đủ (chưa có = null)
        List<CourseScoreDto> scores = allCourses.stream()
                .map(course -> {
                    CourseResult cr = results.stream()
                            .filter(r -> r.getCourse().getCourseId().equals(course.getCourseId()))
                            .findFirst()
                            .orElse(null);

                    return CourseScoreDto.builder()
                            .courseName(course.getCourseName())
                            .theoryScore(cr != null ? cr.getTheoryScore() : null)
                            .practiceScore(cr != null ? cr.getPracticeScore() : null)
                            .attitudeScore(cr != null ? cr.getAttitudeScore() : null)
                            .totalScore(cr != null ? cr.getTotalScore() : null)
                            .build();
                })
                .toList();

        // 4. SummaryResult
        SummaryResult summary = summaryResultRepository
                .findByIntern_InternId(intern.getInternId())
                .orElse(null);

        long trainingDays = calculateWorkingDays(intern.getStartDate(), today);

        Candidate candidate = intern.getCandidate();

        return TrainingDto.builder()
                .internId(intern.getInternId())
                .candidateId(candidate != null ? candidate.getCandidateId() : null)
                .fullName(candidate != null ? candidate.getFullName() : null)
                .startDate(intern.getStartDate())
                .trainingDays(trainingDays)
                .scores(scores) // đủ môn luôn
                .summaryResult(summary != null ? summary.getFinalScore() : null)
                .teamReview(summary != null ? summary.getTeamEvaluation() : null)
                .internshipResult(summary != null ? summary.getInternshipResult() : "NA")
                .internStatus(intern.getInternStatus())
                .build();
    }

    // ============== ĐẾM SỐ TTS ĐÃ BÀN GIAO (PASS & ĐÃ HOÀN THÀNH) THEO KẾ HOẠCH ==============
    public long countInternsDeliveredByPlan(Long planId) {
        return summaryResultRepository
                .countByIntern_RecruitmentPlan_RecruitmentPlanIdAndIntern_InternStatusAndInternshipResult(
                        planId,
                        "Đã hoàn thành",
                        "PASS"
                );
    }

    // ============== NEW: Kiểm tra trạng thái Kế hoạch + Nhu cầu theo internId ==============
    @Transactional
    public void checkRequestAndPlanStatusByInternId(Long internId) {
        Intern intern = internRepository.findById(internId).orElse(null);
        if (intern == null) return;

        RecruitmentPlan plan = intern.getRecruitmentPlan();
        if (plan == null) return;

        HrRequest request = plan.getRequest();
        if (request == null) return;

        // Tổng số lượng nhân sự đầu ra yêu cầu (soLuong)
        int outputRequired = 0;
        if (request.getQuantityCandidates() != null) {
            outputRequired = request.getQuantityCandidates().stream()
                    .mapToInt(q -> q.getSoLuong() != null ? q.getSoLuong() : 0)
                    .sum();
        }

        // Không cấu hình đầu ra thì thôi, không tự chốt
        if (outputRequired <= 0) {
            return;
        }

        Long planId = plan.getRecruitmentPlanId();

        // Số TTS đã bàn giao đủ điều kiện (PASS & ĐÃ HOÀN THÀNH)
        long deliveredCount = countInternsDeliveredByPlan(planId);

        // 1) Đã bàn giao đủ → thành công
        if (deliveredCount >= outputRequired) {
            String planStatus = String.valueOf(plan.getStatus());
            if (!"COMPLETED".equalsIgnoreCase(planStatus)) {
                plan.setStatus("COMPLETED");
                recruitmentPlanRepository.save(plan);
            }

            String reqStatus = String.valueOf(request.getStatus());
            if (!"COMPLETED".equalsIgnoreCase(reqStatus)) {
                request.setStatus("COMPLETED");
                hrRequestRepository.save(request);
            }
            return;
        }

        // 2) Chưa bàn giao đủ → chỉ kết luận khi TẤT CẢ TTS đã được chấm PASS/FAIL
        long totalInterns = internRepository.countByRecruitmentPlan_RecruitmentPlanId(planId);

        long evaluatedInterns = summaryResultRepository
                .countByIntern_RecruitmentPlan_RecruitmentPlanIdAndInternshipResultIn(
                        planId,
                        List.of("PASS", "FAIL")
                );

        if (totalInterns == 0 || evaluatedInterns < totalInterns) {
            // vẫn còn TTS internshipResult = NA → chưa kết luận, để Đang chờ
            return;
        }

        // 3) Tất cả TTS đã chấm, nhưng bàn giao < đầu ra → THẤT BẠI (nhu cầu vẫn COMPLETED)
        String planName = plan.getPlanName() != null ? plan.getPlanName() : ("ID " + planId);

        String reason;
        if (deliveredCount == 0) {
            reason = "Không có thực tập sinh nào đạt yêu cầu để bàn giao cho kế hoạch \"" + planName + "\".";
        } else {
            reason = "Chỉ bàn giao được " + deliveredCount + "/" + outputRequired +
                    " thực tập sinh cho kế hoạch \"" + planName + "\".";
        }

        String formatted = "Lý do: " + reason;

        request.setStatus("COMPLETED");        // nhu cầu đã hoàn thành nhưng kết quả là thất bại
        request.setRejectReason(formatted);    // để FE đọc và hiển thị ở bước Bàn giao nhân sự
        hrRequestRepository.save(request);

        String planStatus = String.valueOf(plan.getStatus());
        if (!"COMPLETED".equalsIgnoreCase(planStatus)) {
            plan.setStatus("COMPLETED");
            recruitmentPlanRepository.save(plan);
        }
    }

    // (OPTIONAL) giữ helper cũ – giờ chỉ gọi sang hàm mới cho đồng bộ
    private void updateRequestAndPlanStatusIfCompleted(Intern intern) {
        if (intern == null || intern.getInternId() == null) return;
        checkRequestAndPlanStatusByInternId(intern.getInternId());
    }

    // ==================== TÍNH NGÀY LÀM VIỆC (T2-T6) ====================
    private long calculateWorkingDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) return 0;

        long days = 0;
        LocalDate date = start;

        while (!date.isAfter(end)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            date = date.plusDays(1);
        }
        return days;
    }
}
    