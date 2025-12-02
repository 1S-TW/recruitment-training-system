// src/main/java/com/example/recruitmenttrainingsystem/service/AIService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.entity.SummaryResult;
import com.example.recruitmenttrainingsystem.entity.Course;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import com.example.recruitmenttrainingsystem.repository.SummaryResultRepository;
import com.example.recruitmenttrainingsystem.repository.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final CourseRepository courseRepository; // 👈 NEW

    // ⭐ Thông điệp fallback cho mọi trường hợp không hiểu / lỗi
    private static final String FALLBACK_MESSAGE =
            "Bé chưa hiểu câu hỏi của anh/chị ạ, anh/chị hãy ghi rõ câu hỏi hơn giúp bé với ạ ❤️";

    public AIService(GroqClient groqClient,
                     ObjectMapper objectMapper,
                     InternRepository internRepository,
                     SummaryResultRepository summaryResultRepository,
                     CourseRepository courseRepository) { // 👈 NEW param
        this.groqClient = groqClient;
        this.objectMapper = objectMapper;
        this.internRepository = internRepository;
        this.summaryResultRepository = summaryResultRepository;
        this.courseRepository = courseRepository; // 👈 NEW
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
        long startDay; // ngày bắt đầu (từ 1)
        long endDay;   // ngày kết thúc

        CourseTimeline(String courseName, long startDay, long endDay) {
            this.courseName = courseName;
            this.startDay = startDay;
            this.endDay = endDay;
        }
    }

    /**
     * Xây dựng timeline dựa trên bảng course:
     * - Lấy đúng 5 môn theo thứ tự:
     *   Git & GitHub -> OOP -> SQL -> Web cơ bản -> Java Core & Spring Boot
     * - Khoảng ngày cho từng môn dựa trên duration_days
     *   Ví dụ: 3,4,4,5,6  =>  [1-3], [4-7], [8-11], [12-16], [17-22]
     */
    private List<CourseTimeline> buildCourseTimeline() {
        // Thứ tự cố định
        List<String> orderedNames = List.of(
                "Git & GitHub",
                "Lập trình hướng đối tượng (OOP)",
                "Cơ sở dữ liệu (SQL)",
                "Web cơ bản (HTML - CSS - JavaScript)",
                "Java Core & Spring Boot"
        );

        // Map tên môn -> duration_days
        Map<String, Integer> durationMap = courseRepository.findAll()
                .stream()
                .filter(c -> c.getCourseName() != null)
                .collect(Collectors.toMap(
                        Course::getCourseName,
                        c -> Optional.ofNullable(c.getDurationDays()).orElse(0),
                        (a, b) -> a
                ));

        List<CourseTimeline> result = new ArrayList<>();
        long currentStart = 1;

        for (String name : orderedNames) {
            Integer d = durationMap.get(name);
            if (d == null || d <= 0) {
                // Nếu môn chưa có hoặc duration_days <= 0 -> bỏ qua
                continue;
            }
            long start = currentStart;
            long end = currentStart + d - 1;
            result.add(new CourseTimeline(name, start, end));
            currentStart = end + 1;
        }
        return result;
    }

    // Tìm tên môn tương ứng với số ngày thực tập
    private String findCourseNameForDay(List<CourseTimeline> timeline, long trainingDays) {
        if (timeline == null || timeline.isEmpty()) {
            return "Không rõ môn hiện tại";
        }
        for (CourseTimeline c : timeline) {
            if (trainingDays <= c.endDay) {
                return c.courseName;
            }
        }
        // Nếu vượt quá toàn bộ timeline thì coi là đang ở môn cuối
        return timeline.get(timeline.size() - 1).courseName;
    }

    // Tính số ngày làm việc (T2–T6)
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

    /**
     * Thống kê các TTS CHẬM TIẾN ĐỘ theo từng kế hoạch:
     * - Lấy mốc chuẩn = tổng duration_days của các môn trong timeline
     * - Với từng intern:
     *   + Lấy internship_days; nếu null thì tính lại theo start_date (ngày làm việc)
     *   + Nếu internStatus = "Đang thực tập" và số ngày > mốc chuẩn
     *     => coi là chậm tiến độ
     *   + Xác định "môn hiện tại" theo timeline để hiển thị
     */
    private String handleSlowProgressByPlans() {
        List<CourseTimeline> timeline = buildCourseTimeline();
        if (timeline.isEmpty()) {
            return "Hiện chưa cấu hình đủ 'Số ngày học' cho các môn nên bé chưa đánh giá được tiến độ thực tập sinh ạ.";
        }

        long totalStandardDays = timeline.get(timeline.size() - 1).endDay;

        // class nhỏ để hiển thị
        class InternProgress {
            String fullName;
            String currentCourse;
            long trainingDays;

            InternProgress(String fullName, String currentCourse, long trainingDays) {
                this.fullName = fullName;
                this.currentCourse = currentCourse;
                this.trainingDays = trainingDays;
            }
        }

        Map<RecruitmentPlan, List<InternProgress>> map = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (Intern intern : internRepository.findAll()) {
            if (!"Đang thực tập".equalsIgnoreCase(intern.getInternStatus())) {
                continue;
            }

            long trainingDays;
            if (intern.getInternshipDays() != null && intern.getInternshipDays() > 0) {
                trainingDays = intern.getInternshipDays();
            } else {
                LocalDate startDate = intern.getStartDate();
                LocalDate endDate = intern.getEndDate() != null ? intern.getEndDate() : today;
                trainingDays = calculateWorkingDays(startDate, endDate);
            }

            // Chỉ xét những bạn bị CHẬM (số ngày > tổng chuẩn)
            if (trainingDays <= totalStandardDays) {
                continue;
            }

            String courseName = findCourseNameForDay(timeline, trainingDays);

            String fullName = intern.getCandidate() != null
                    ? intern.getCandidate().getFullName()
                    : ("Intern #" + intern.getInternId());

            RecruitmentPlan plan = intern.getRecruitmentPlan();
            map.computeIfAbsent(plan, k -> new ArrayList<>())
                    .add(new InternProgress(fullName, courseName, trainingDays));
        }

        if (map.isEmpty()) {
            return "Hiện tại không có thực tập sinh nào bị chậm tiến độ so với tổng "
                    + totalStandardDays + " ngày của 5 môn học ạ.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Chào anh/chị 👋\n\n");
        sb.append("Dưới đây là các kế hoạch tuyển dụng đang có thực tập sinh CHẬM TIẾN ĐỘ ")
                .append("(so với mốc ").append(totalStandardDays).append(" ngày cho 5 môn):\n\n");

        for (Map.Entry<RecruitmentPlan, List<InternProgress>> entry : map.entrySet()) {
            RecruitmentPlan plan = entry.getKey();
            String planTitle;
            if (plan == null) {
                planTitle = "Không gắn với kế hoạch nào";
            } else if (plan.getPlanName() != null) {
                planTitle = plan.getPlanName();
            } else {
                planTitle = "Kế hoạch #" + plan.getRecruitmentPlanId();
            }

            List<InternProgress> list = entry.getValue();

            sb.append("Kế hoạch\n");
            sb.append(planTitle).append("\n");
            sb.append(list.size()).append(" bạn chậm tiến độ\n");
            sb.append("STT\tTên TTS\tMôn hiện tại\tSố ngày TT\n");

            int stt = 1;
            for (InternProgress ip : list) {
                sb.append(stt++).append("\t")
                        .append(ip.fullName).append("\t")
                        .append(ip.currentCourse).append("\t")
                        .append(ip.trainingDays).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
