package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.entity.SummaryResult;
import com.example.recruitmenttrainingsystem.entity.Course;
import com.example.recruitmenttrainingsystem.entity.CourseResult;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import com.example.recruitmenttrainingsystem.repository.SummaryResultRepository;
import com.example.recruitmenttrainingsystem.repository.CourseRepository;
import com.example.recruitmenttrainingsystem.repository.CourseResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIService {

    // Vẫn giữ để Spring inject cho đúng constructor cũ,
    // nhưng hiện tại MÌNH KHÔNG GỌI Groq nữa.
    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    private final InternRepository internRepository;
    private final SummaryResultRepository summaryResultRepository;
    private final CourseRepository courseRepository;
    private final CourseResultRepository courseResultRepository;   // 👈 NEW

    // ⭐ Thông điệp fallback cho mọi trường hợp không hiểu / lỗi
    private static final String FALLBACK_MESSAGE =
            "Bé chưa hiểu câu hỏi của anh/chị ạ, anh/chị hãy ghi rõ câu hỏi hơn giúp bé với ạ ❤️";

    public AIService(GroqClient groqClient,
                     ObjectMapper objectMapper,
                     InternRepository internRepository,
                     SummaryResultRepository summaryResultRepository,
                     CourseRepository courseRepository,
                     CourseResultRepository courseResultRepository) {   // 👈 thêm tham số
        this.groqClient = groqClient;
        this.objectMapper = objectMapper;
        this.internRepository = internRepository;
        this.summaryResultRepository = summaryResultRepository;
        this.courseRepository = courseRepository;
        this.courseResultRepository = courseResultRepository;         // 👈 gán field
    }

    /**
     * Hàm chat dùng cho endpoint /api/ai/chat
     * - Nếu nhận diện được câu hỏi “đặc biệt” thì xử lý trực tiếp từ DB
     * - Còn lại trả về FALLBACK_MESSAGE (không gọi Groq nữa).
     */
    public String chat(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return FALLBACK_MESSAGE;
        }

        String normalized = userMessage.trim().toLowerCase();

        try {
            // 1) Thống kê trạng thái thực tập sinh
            if (normalized.contains("thống kê trạng thái tts")) {
                return handleStatusStatistics();
            }

            // 2) Tổng số TTS
            if (normalized.contains("tổng số tts")) {
                return handleTotalInterns();
            }

            // 3) Thống kê kết quả thực tập (PASS/FAIL)
            if (normalized.contains("thống kê kết quả thực tập")) {
                return handlePassFailStatistics();
            }

            // 4) Điểm trung bình theo từ khóa kế hoạch
            if (normalized.contains("điểm trung bình")
                    && normalized.contains("từ khóa")) {
                String keyword = extractKeyword(userMessage);
                return handleAverageScoreByPlanKeyword(keyword);
            }

            // 5) Thống kê TTS chậm tiến độ (dựa trên duration_days trong bảng course)
            if (normalized.contains("chậm tiến độ")
                    || normalized.contains("cham tien do")) {
                return handleSlowProgressByPlans();
            }

            // Không khớp rule nào -> trả về fallback tiếng Việt
            return FALLBACK_MESSAGE;

        } catch (Exception ex) {
            // Nếu logic bên trên lỗi -> fallback
            return FALLBACK_MESSAGE;
        }
    }

    // ====================== 1. THỐNG KÊ TRẠNG THÁI TTS ======================

    private String handleStatusStatistics() {
        List<Intern> all = internRepository.findAll();

        long total = all.size();
        long dangThucTap = all.stream()
                .filter(i -> "Đang thực tập".equalsIgnoreCase(i.getInternStatus()))
                .count();
        long daHoanThanh = all.stream()
                .filter(i -> "Đã hoàn thành".equalsIgnoreCase(i.getInternStatus()))
                .count();
        long daDung = all.stream()
                .filter(i -> "Đã dừng thực tập".equalsIgnoreCase(i.getInternStatus()))
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("Thống kê thực tập sinh theo trạng thái hiện tại:\n\n");
        sb.append("- Tổng số TTS: ").append(total).append("\n");
        sb.append("- Đang thực tập: ").append(dangThucTap).append("\n");
        sb.append("- Đã hoàn thành: ").append(daHoanThanh).append("\n");
        sb.append("- Đã dừng thực tập: ").append(daDung);

        return sb.toString();
    }

    // ====================== 2. TỔNG SỐ TTS ======================

    private String handleTotalInterns() {
        long total = internRepository.count();
        return "Hiện tại hệ thống đang có tổng cộng " + total + " thực tập sinh (TTS) ạ.";
    }

    // ====================== 3. THỐNG KÊ PASS/FAIL ======================

    private String handlePassFailStatistics() {
        List<SummaryResult> all = summaryResultRepository.findAll();
        if (all.isEmpty()) {
            return "Hiện tại chưa có dữ liệu kết quả thực tập nào trong hệ thống ạ.";
        }

        long pass = all.stream()
                .filter(s -> "PASS".equalsIgnoreCase(s.getInternshipResult()))
                .count();
        long fail = all.stream()
                .filter(s -> "FAIL".equalsIgnoreCase(s.getInternshipResult()))
                .count();
        long na = all.stream()
                .filter(s -> {
                    String r = s.getInternshipResult();
                    return r == null
                            || (!"PASS".equalsIgnoreCase(r) && !"FAIL".equalsIgnoreCase(r));
                })
                .count();

        long totalWithResult = pass + fail + na;

        StringBuilder sb = new StringBuilder();
        sb.append("Thống kê kết quả thực tập theo PASS/FAIL:\n\n");
        sb.append("- Tổng số TTS có bản ghi kết quả: ").append(totalWithResult).append("\n");
        sb.append("- PASS: ").append(pass).append("\n");
        sb.append("- FAIL: ").append(fail).append("\n");
        sb.append("- Chưa có kết quả / NA: ").append(na);

        return sb.toString();
    }

    // ====================== 4. ĐIỂM TB THEO TỪ KHÓA KẾ HOẠCH ======================

    /**
     * Tách keyword từ câu kiểu:
     * "điểm trung bình ... trong kế hoạch có từ khóa "qq""
     */
    private String extractKeyword(String raw) {
        if (raw == null) return null;

        // Ưu tiên bắt trong ngoặc kép
        Pattern pQuoted = Pattern.compile("từ khóa\\s+\"([^\"]+)\"", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher mQuoted = pQuoted.matcher(raw);
        if (mQuoted.find()) {
            return mQuoted.group(1).trim();
        }

        // Không có ngoặc kép thì cắt phần sau "từ khóa"
        String lower = raw.toLowerCase();
        int idx = lower.indexOf("từ khóa");
        if (idx >= 0) {
            String tail = raw.substring(idx + "từ khóa".length()).trim();
            if (tail.startsWith(":")) tail = tail.substring(1).trim();
            return tail;
        }

        return null;
    }

    private String handleAverageScoreByPlanKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "Bé chưa rõ anh/chị muốn xem kế hoạch nào, anh/chị hãy ghi rõ từ khóa kế hoạch giúp bé với ạ ❤️";
        }

        String kwLower = keyword.toLowerCase();

        // 1) Lấy tất cả intern thuộc các kế hoạch có tên chứa keyword
        List<Intern> internsInPlans = internRepository.findAll().stream()
                .filter(i -> {
                    RecruitmentPlan p = i.getRecruitmentPlan();
                    if (p == null) return false;
                    String name = p.getPlanName();
                    return name != null && name.toLowerCase().contains(kwLower);
                })
                .collect(Collectors.toList());

        if (internsInPlans.isEmpty()) {
            return "Hiện chưa tìm thấy kế hoạch tuyển dụng nào có từ khóa \"" + keyword + "\" ạ.";
        }

        long totalInterns = internsInPlans.size();

        // Lấy danh sách internId
        Set<Long> internIds = internsInPlans.stream()
                .map(Intern::getInternId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2) Lấy SummaryResult của các intern đó
        List<SummaryResult> allResults = summaryResultRepository.findAll().stream()
                .filter(sr -> sr.getIntern() != null
                        && internIds.contains(sr.getIntern().getInternId()))
                .collect(Collectors.toList());

        // 3) Lọc những bạn đã hoàn thành & PASS & có finalScore
        List<SummaryResult> completedPass = allResults.stream()
                .filter(sr -> {
                    Intern intern = sr.getIntern();
                    if (intern == null) return false;

                    String status = intern.getInternStatus();
                    String result = sr.getInternshipResult();
                    BigDecimal finalScore = sr.getFinalScore();

                    return "Đã hoàn thành".equalsIgnoreCase(status)
                            && "PASS".equalsIgnoreCase(result)
                            && finalScore != null;
                })
                .collect(Collectors.toList());

        if (completedPass.isEmpty()) {
            return "Trong các kế hoạch có từ khóa \"" + keyword +
                    "\" hiện chưa có thực tập sinh nào đã hoàn thành (PASS) và có điểm Tổng kết, nên chưa thể tính điểm trung bình ạ.";
        }

        // 4) Tính điểm trung bình
        BigDecimal sum = completedPass.stream()
                .map(SummaryResult::getFinalScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avg = sum.divide(
                BigDecimal.valueOf(completedPass.size()),
                2,
                RoundingMode.HALF_UP
        );

        long completedCount = completedPass.size();

        // Danh sách tên kế hoạch khớp keyword
        Set<String> planNames = internsInPlans.stream()
                .map(Intern::getRecruitmentPlan)
                .filter(Objects::nonNull)
                .map(RecruitmentPlan::getPlanName)
                .filter(Objects::nonNull)
                .filter(name -> name.toLowerCase().contains(kwLower))
                .collect(Collectors.toCollection(TreeSet::new));

        StringBuilder sb = new StringBuilder();
        sb.append("Kết quả điểm trung bình cho các kế hoạch có từ khóa \"")
                .append(keyword)
                .append("\":\n\n");

        sb.append("- Các kế hoạch khớp từ khóa: ");
        if (planNames.isEmpty()) {
            sb.append("(không rõ tên)\n");
        } else {
            sb.append(String.join(", ", planNames)).append("\n");
        }

        sb.append("- Tổng số TTS trong các kế hoạch này: ").append(totalInterns).append("\n");
        sb.append("- Số TTS đã hoàn thành & PASS và có điểm Tổng kết: ")
                .append(completedCount)
                .append("\n");

        if (completedCount < totalInterns) {
            sb.append("→ Điểm trung bình được tính trên ")
                    .append(completedCount)
                    .append("/")
                    .append(totalInterns)
                    .append(" TTS đã hoàn thành các môn học.\n");
        }

        sb.append("\n=> Điểm trung bình Tổng kết: ")
                .append(avg)
                .append(" điểm.");

        return sb.toString();
    }

    // ====================== 5. TIẾN ĐỘ THEO SỐ NGÀY HỌC MỖI MÔN ======================

    // Timeline đơn giản cho một môn
    private static class CourseTimeline {
        String courseName;
        Long courseId;
        long startDay; // ngày bắt đầu (từ 1)
        long endDay;   // ngày kết thúc

        CourseTimeline(String courseName, Long courseId, long startDay, long endDay) {
            this.courseName = courseName;
            this.courseId = courseId;
            this.startDay = startDay;
            this.endDay = endDay;
        }
    }

    /**
     * Xây dựng timeline dựa trên bảng course:
     * - Lấy MỌI môn trong DB, sort theo displayOrder rồi courseId
     * - Dùng duration_days để tính khoảng ngày cho từng môn:
     *   Ví dụ: 3,4,4,5,6  =>  [1-3], [4-7], [8-11], [12-16], [17-22]
     */
    private List<CourseTimeline> buildCourseTimeline() {
        List<Course> courses = courseRepository.findAll(
                Sort.by(Sort.Direction.ASC, "displayOrder", "courseId")
        );

        List<CourseTimeline> result = new ArrayList<>();
        long currentStart = 1;

        for (Course c : courses) {
            Integer d = c.getDurationDays();
            if (d == null || d <= 0) {
                continue; // bỏ các môn chưa set số ngày học
            }

            long start = currentStart;
            long end = currentStart + d - 1;

            result.add(new CourseTimeline(
                    c.getCourseName(),
                    c.getCourseId(),
                    start,
                    end
            ));

            currentStart = end + 1;
        }

        return result;
    }

    // Trạng thái tiến độ cho 1 TTS
    private static class InternProgressStatus {
        String currentCourseName;       // đang "đứng" ở môn nào trong chương trình
        long trainingDays;             // số ngày thực tập (T2–T6)
        long expectedCompletedCourses; // số môn LẼ RA phải hoàn thành
        long actualCompletedCourses;   // số môn thực tế đã hoàn thành (tuần tự)

        boolean isSlow()   { return actualCompletedCourses < expectedCompletedCourses; }
        boolean isFast()   { return actualCompletedCourses > expectedCompletedCourses; }
        boolean isOnTrack(){ return actualCompletedCourses == expectedCompletedCourses; }
    }

    /**
     * Đánh giá tiến độ 1 TTS:
     * - Tính trainingDays (ưu tiên internship_days, nếu thiếu thì tính theo start_date → hôm nay)
     * - Từ trainingDays → expectedCompletedCourses (bao nhiêu môn lẽ ra phải xong)
     * - Lấy CourseResult → actualCompletedCourses (bao nhiêu môn đầu tiên đã đủ 3 điểm)
     * - Không cho "nhảy cóc": gặp 1 môn chưa xong thì các môn sau không tính.
     */
    private InternProgressStatus evaluateInternProgress(Intern intern,
                                                        List<CourseTimeline> timeline) {
        if (intern == null || intern.getInternId() == null) return null;
        if (timeline == null || timeline.isEmpty()) return null;

        Long internId = intern.getInternId();

        // 1. Tính số ngày thực tập
        LocalDate today = LocalDate.now();
        LocalDate endDate = intern.getEndDate() != null ? intern.getEndDate() : today;

        long trainingDays;
        if (intern.getInternshipDays() != null && intern.getInternshipDays() > 0) {
            trainingDays = intern.getInternshipDays();
        } else {
            trainingDays = calculateWorkingDays(intern.getStartDate(), endDate);
        }

        // 2. Lấy toàn bộ CourseResult của intern này
        List<CourseResult> results = courseResultRepository.findByIntern_InternId(internId);

        // Map courseId -> đã hoàn thành (đủ 3 điểm)
        Set<Long> completedCourseIds = results.stream()
                .filter(cr -> cr.getCourse() != null)
                .filter(cr -> cr.getTheoryScore() != null
                        && cr.getPracticeScore() != null
                        && cr.getAttitudeScore() != null)
                .map(cr -> cr.getCourse().getCourseId())
                .collect(Collectors.toSet());

        // 3. Số môn LẼ RA phải xong theo số ngày
        long expectedCompleted = timeline.stream()
                .filter(ct -> trainingDays >= ct.endDay)
                .count();

        // 4. Số môn thực tế đã hoàn thành nhưng bắt buộc tuần tự
        long actualCompleted = 0;
        for (CourseTimeline ct : timeline) {
            if (completedCourseIds.contains(ct.courseId)) {
                actualCompleted++;
            } else {
                // gặp 1 môn chưa xong thì coi như các môn sau cũng chưa được tính
                break;
            }
        }

        // 5. Xác định "môn hiện tại" để hiển thị
        int currentIndex;
        if (actualCompleted >= timeline.size()) {
            currentIndex = timeline.size() - 1;
        } else {
            currentIndex = (int) actualCompleted;
        }
        if (currentIndex < 0) currentIndex = 0;

        String currentCourseName = timeline.get(currentIndex).courseName;

        InternProgressStatus status = new InternProgressStatus();
        status.currentCourseName = currentCourseName;
        status.trainingDays = trainingDays;
        status.expectedCompletedCourses = expectedCompleted;
        status.actualCompletedCourses = actualCompleted;

        return status;
    }

    /**
     * Thống kê các TTS CHẬM TIẾN ĐỘ theo từng kế hoạch:
     * - Dựa trên duration_days từng môn (timeline) + điểm thành phần từng môn trong course_result
     * - Không cho học nhảy cóc: phải hoàn thành môn A rồi mới tính đến B, C, ...
     */
    private String handleSlowProgressByPlans() {
        List<CourseTimeline> timeline = buildCourseTimeline();
        if (timeline.isEmpty()) {
            return "Hiện chưa cấu hình 'Số ngày học' cho các môn nên bé chưa đánh giá được tiến độ thực tập sinh ạ.";
        }

        // dùng cho hiển thị
        class InternRow {
            String fullName;
            String currentCourseName;
            long trainingDays;
            long expectedCompleted;
            long actualCompleted;

            InternRow(String fullName,
                      String currentCourseName,
                      long trainingDays,
                      long expectedCompleted,
                      long actualCompleted) {
                this.fullName = fullName;
                this.currentCourseName = currentCourseName;
                this.trainingDays = trainingDays;
                this.expectedCompleted = expectedCompleted;
                this.actualCompleted = actualCompleted;
            }
        }

        Map<RecruitmentPlan, List<InternRow>> map = new LinkedHashMap<>();

        for (Intern intern : internRepository.findAll()) {
            if (!"Đang thực tập".equalsIgnoreCase(intern.getInternStatus())) {
                continue;
            }

            InternProgressStatus status = evaluateInternProgress(intern, timeline);
            if (status == null || !status.isSlow()) {
                continue; // chỉ quan tâm những bạn CHẬM
            }

            String fullName = intern.getCandidate() != null
                    ? intern.getCandidate().getFullName()
                    : ("Intern #" + intern.getInternId());

            RecruitmentPlan plan = intern.getRecruitmentPlan();

            map.computeIfAbsent(plan, k -> new ArrayList<>())
                    .add(new InternRow(
                            fullName,
                            status.currentCourseName,
                            status.trainingDays,
                            status.expectedCompletedCourses,
                            status.actualCompletedCourses
                    ));
        }

        if (map.isEmpty()) {
            return "Hiện tại không có thực tập sinh nào bị chậm tiến độ so với số ngày học của các môn trong chương trình ạ.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Chào anh/chị 👋\n\n");
        sb.append("Dưới đây là các kế hoạch tuyển dụng đang có thực tập sinh CHẬM TIẾN ĐỘ ")
                .append("(dựa trên số ngày học từng môn trong chương trình đào tạo):\n\n");

        for (Map.Entry<RecruitmentPlan, List<InternRow>> entry : map.entrySet()) {
            RecruitmentPlan plan = entry.getKey();
            List<InternRow> rows = entry.getValue();

            String planTitle;
            if (plan == null) {
                planTitle = "Không gắn với kế hoạch nào";
            } else if (plan.getPlanName() != null) {
                planTitle = plan.getPlanName();
            } else {
                planTitle = "Kế hoạch #" + plan.getRecruitmentPlanId();
            }

            sb.append("Kế hoạch: ").append(planTitle).append("\n");
            sb.append(rows.size()).append(" bạn chậm tiến độ\n");
            sb.append("STT\tTên TTS\tMôn hiện tại\tSố ngày TT\tMôn lẽ ra phải xong\tMôn đã xong\n");

            int stt = 1;
            for (InternRow r : rows) {
                sb.append(stt++).append("\t")
                        .append(r.fullName).append("\t")
                        .append(r.currentCourseName).append("\t")
                        .append(r.trainingDays).append("\t")
                        .append(r.expectedCompleted).append("\t")
                        .append(r.actualCompleted)
                        .append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
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
