// src/main/java/com/example/recruitmenttrainingsystem/service/TrainingService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.dto.*;
import com.example.recruitmenttrainingsystem.entity.*;
import com.example.recruitmenttrainingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // ĐÃ THÊM DÒNG NÀY!

@Service
@RequiredArgsConstructor
@Transactional
public class TrainingService {

    private final InternRepository internRepository;
    private final CourseRepository courseRepository;
    private final CourseResultRepository courseResultRepository;
    private final SummaryResultRepository summaryResultRepository;
    private final CourseScoreHistoryRepository courseScoreHistoryRepository;
    private final RecruitmentPlanRepository recruitmentPlanRepository;
    private final HrRequestRepository hrRequestRepository;

    // ==================== GET ALL ====================
    public List<TrainingDto> getAll() {
        return internRepository.findAll().stream()
                .map(this::toTrainingDto)
                .toList();
    }

    public List<TrainingDto> getByPlan(Long planId) {
        if (planId == null) return List.of();
        return internRepository.findAll().stream()
                .filter(intern -> intern.getRecruitmentPlan() != null &&
                        planId.equals(intern.getRecruitmentPlan().getRecruitmentPlanId()))
                .map(this::toTrainingDto)
                .toList();
    }

    // ==================== UPDATE SCORES ====================
    @Transactional
    public TrainingDto updateScores(Long internId, TrainingScoreDto dto) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thực tập sinh ID: " + internId));

        // Cập nhật điểm từng môn (giữ nguyên logic 3 lần chấm)
        if (dto.getScores() != null && !dto.getScores().isEmpty()) {
            for (CourseScoreDto s : dto.getScores()) {
                updateSingleCourseScore(intern, s);
            }
        }

        SummaryResult summary = summaryResultRepository.findByIntern_InternId(internId)
                .orElseGet(() -> SummaryResult.builder()
                        .intern(intern)
                        .internshipResult("Chưa kết luận")
                        .build());

        summary.setFinalScore(dto.getSummaryResult());
        summary.setTeamEvaluation(dto.getTeamReview());

        String newStatus = "Đang thực tập";
        LocalDate endDate = null;

        if ("Đã dừng thực tập".equals(intern.getInternStatus())) {
            newStatus = "Đã dừng thực tập";
        } else {
            boolean allCompleted = courseResultRepository.findByIntern_InternId(internId).stream()
                    .allMatch(cr -> cr.getTheoryScore() != null &&
                            cr.getPracticeScore() != null &&
                            cr.getAttitudeScore() != null);

            if (allCompleted) {
                newStatus = "Đã hoàn thành";
                endDate = LocalDate.now();
                intern.setEndDate(endDate);

                // SỬA LỖI: Chuyển BigDecimal → Double
                BigDecimal avgBigDecimal = dto.getSummaryResult();
                Double avg = (avgBigDecimal != null) ? avgBigDecimal.doubleValue() : null;

                boolean hasSubjectBelow7 = courseResultRepository.findByIntern_InternId(internId).stream()
                        .anyMatch(cr -> cr.getTotalScore() != null &&
                                cr.getTotalScore().compareTo(BigDecimal.valueOf(7)) < 0);

                String autoResult = (avg != null && avg >= 7 && !hasSubjectBelow7) ? "Đạt" : "Không đạt";
                summary.setInternshipResult(autoResult);
            } else {
                summary.setInternshipResult("Chưa kết luận");
            }
        }

        summaryResultRepository.save(summary);
        intern.setInternStatus(newStatus);
        internRepository.save(intern);

        checkRequestAndPlanStatusByInternId(internId);

        return toTrainingDto(intern);
    }

    private void updateSingleCourseScore(Intern intern, CourseScoreDto s) {
        Course course = courseRepository.findByCourseName(s.getCourseName())
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại: " + s.getCourseName()));

        CourseResult cr = courseResultRepository
                .findByIntern_InternIdAndCourse_CourseName(intern.getInternId(), course.getCourseName())
                .orElseGet(() -> {
                    CourseResult newCr = CourseResult.builder()
                            .intern(intern)
                            .course(course)
                            .build();
                    return courseResultRepository.save(newCr);
                });

        int currentAttempts = courseScoreHistoryRepository.countByCourseResult(cr);

        if (currentAttempts >= 3) {
            throw new IllegalArgumentException("Môn " + s.getCourseName() + " đã chấm đủ 3 lần, không thể chấm thêm!");
        }

        if (currentAttempts > 0) {
            List<CourseScoreHistory> historyList = courseScoreHistoryRepository
                    .findByCourseResult_CourseResultIdOrderByAttemptNumberAsc(cr.getCourseResultId());
            if (!historyList.isEmpty()) {
                CourseScoreHistory last = historyList.get(historyList.size() - 1);
                BigDecimal lastTotal = calculateTotalScore(last.getTheoryScore(), last.getPracticeScore(), last.getAttitudeScore());
                if (lastTotal != null && lastTotal.compareTo(BigDecimal.valueOf(7)) >= 0) {
                    return;
                }
            }
        }

        if (s.getTheoryScore() == null || s.getPracticeScore() == null || s.getAttitudeScore() == null) {
            throw new IllegalArgumentException("Phải nhập đủ 3 loại điểm cho môn " + s.getCourseName());
        }

        BigDecimal newTotal = s.getTheoryScore()
                .add(s.getPracticeScore())
                .add(s.getAttitudeScore())
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);

        if (newTotal.compareTo(BigDecimal.valueOf(7)) < 0) {
            if (s.getReason() == null || s.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Điểm môn " + s.getCourseName() + " = " + newTotal + " < 7 → Bắt buộc phải nhập lý do!");
            }
        }

        CourseScoreHistory history = CourseScoreHistory.builder()
                .courseResult(cr)
                .attemptNumber(currentAttempts + 1)
                .theoryScore(s.getTheoryScore())
                .practiceScore(s.getPracticeScore())
                .attitudeScore(s.getAttitudeScore())
                .reason(newTotal.compareTo(BigDecimal.valueOf(7)) < 0 ? s.getReason() : null)
                .build();
        courseScoreHistoryRepository.save(history);

        cr.setTheoryScore(s.getTheoryScore());
        cr.setPracticeScore(s.getPracticeScore());
        cr.setAttitudeScore(s.getAttitudeScore());
        cr.setTotalScore(newTotal);
        courseResultRepository.save(cr);
    }

    private BigDecimal calculateTotalScore(BigDecimal t, BigDecimal p, BigDecimal a) {
        if (t == null || p == null || a == null) return null;
        return t.add(p).add(a).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
    }

    public TrainingDto toTrainingDto(Intern intern) {
        List<Course> allCourses = courseRepository.findAll();

        List<CourseScoreDto> scores = allCourses.stream()
                .map(course -> {
                    Optional<CourseResult> crOpt = courseResultRepository
                            .findByIntern_InternIdAndCourse_CourseName(intern.getInternId(), course.getCourseName());

                    CourseResult cr = crOpt.orElse(null);

                    List<CourseScoreHistory> histories = cr != null
                            ? courseScoreHistoryRepository
                            .findByCourseResult_CourseResultIdOrderByAttemptNumberAsc(cr.getCourseResultId())
                            : List.of();

                    List<CourseScoreHistoryDto> historyDtos = histories.stream()
                            .map(h -> CourseScoreHistoryDto.builder()
                                    .attemptNumber(h.getAttemptNumber())
                                    .theoryScore(h.getTheoryScore())
                                    .practiceScore(h.getPracticeScore())
                                    .attitudeScore(h.getAttitudeScore())
                                    .totalScore(calculateTotalScore(h.getTheoryScore(), h.getPracticeScore(), h.getAttitudeScore()))
                                    .reason(h.getReason())
                                    .build())
                            .toList();

                    int totalAttempts = histories.size();
                    int remainingAttempts = 3 - totalAttempts;

                    String latestReason = histories.isEmpty() ? null : histories.get(histories.size() - 1).getReason();

                    return CourseScoreDto.builder()
                            .courseName(course.getCourseName())
                            .theoryScore(cr != null ? cr.getTheoryScore() : null)
                            .practiceScore(cr != null ? cr.getPracticeScore() : null)
                            .attitudeScore(cr != null ? cr.getAttitudeScore() : null)
                            .totalScore(cr != null ? cr.getTotalScore() : null)
                            .reason(latestReason)
                            .history(historyDtos)
                            .totalAttempts(totalAttempts)
                            .remainingAttempts(remainingAttempts)
                            .build();
                })
                .toList();

        SummaryResult summary = summaryResultRepository.findByIntern_InternId(intern.getInternId()).orElse(null);

        LocalDate today = LocalDate.now();
        LocalDate endDate = intern.getEndDate() != null ? intern.getEndDate() : today;
        long trainingDays = calculateWorkingDays(intern.getStartDate(), endDate);
        intern.setInternshipDays((int) trainingDays);

        Candidate candidate = intern.getCandidate();

        return TrainingDto.builder()
                .internId(intern.getInternId())
                .candidateId(candidate != null ? candidate.getCandidateId() : null)
                .recruitmentPlanId(intern.getRecruitmentPlan() != null ? intern.getRecruitmentPlan().getRecruitmentPlanId() : null)
                .fullName(candidate != null ? candidate.getFullName() : null)
                .startDate(intern.getStartDate())
                .endDate(intern.getEndDate())
                .trainingDays(trainingDays)
                .scores(scores)
                .summaryResult(summary != null ? summary.getFinalScore() : null)
                .teamReview(summary != null ? summary.getTeamEvaluation() : null)
                .internshipResult(summary != null && summary.getInternshipResult() != null
                        ? summary.getInternshipResult()
                        : "Chưa kết luận")
                .internStatus(intern.getInternStatus())
                .build();
    }

    public long countInternsDeliveredByPlan(Long planId) {
        return summaryResultRepository
                .countByIntern_RecruitmentPlan_RecruitmentPlanIdAndIntern_InternStatusAndInternshipResult(
                        planId, "Đã hoàn thành", "Đạt");
    }

    @Transactional
    public void checkRequestAndPlanStatusByInternId(Long internId) {
        Intern intern = internRepository.findById(internId).orElse(null);
        if (intern == null) return;
        RecruitmentPlan plan = intern.getRecruitmentPlan();
        if (plan == null) return;
        HrRequest request = plan.getRequest();
        if (request == null) return;

        int outputRequired = request.getQuantityCandidates() != null ?
                request.getQuantityCandidates().stream()
                        .mapToInt(q -> q.getSoLuong() != null ? q.getSoLuong() : 0)
                        .sum() : 0;

        if (outputRequired <= 0) return;

        Long planId = plan.getRecruitmentPlanId();
        long deliveredCount = countInternsDeliveredByPlan(planId);

        if (deliveredCount >= outputRequired) {
            plan.setStatus("COMPLETED");
            recruitmentPlanRepository.save(plan);
            request.setStatus("COMPLETED");
            hrRequestRepository.save(request);
            return;
        }

        long totalInterns = internRepository.countByRecruitmentPlan_RecruitmentPlanId(planId);
        long evaluatedInterns = summaryResultRepository
                .countByIntern_RecruitmentPlan_RecruitmentPlanIdAndInternshipResultIn(planId, List.of("Đạt", "Không đạt"));

        if (totalInterns == 0 || evaluatedInterns < totalInterns) return;

        String planName = plan.getPlanName() != null ? plan.getPlanName() : ("ID " + planId);
        String reason = deliveredCount == 0
                ? "Không có thực tập sinh nào đạt yêu cầu để bàn giao cho kế hoạch \"" + planName + "\"."
                : "Chỉ bàn giao được " + deliveredCount + "/" + outputRequired + " thực tập sinh cho kế hoạch \"" + planName + "\".";

        request.setStatus("COMPLETED");
        request.setRejectReason("Lý do: " + reason);
        hrRequestRepository.save(request);
        plan.setStatus("COMPLETED");
        recruitmentPlanRepository.save(plan);
    }

    private long calculateWorkingDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) return 0;
        long days = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) days++;
            date = date.plusDays(1);
        }
        return days;
    }
}
