// src/main/java/com/example/recruitmenttrainingsystem/service/AIService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.entity.Intern;
import com.example.recruitmenttrainingsystem.entity.RecruitmentPlan;
import com.example.recruitmenttrainingsystem.entity.SummaryResult;
import com.example.recruitmenttrainingsystem.repository.InternRepository;
import com.example.recruitmenttrainingsystem.repository.SummaryResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // ⭐ Thông điệp fallback cho mọi trường hợp không hiểu / lỗi
    private static final String FALLBACK_MESSAGE =
            "Bé chưa hiểu câu hỏi của anh/chị ạ, anh/chị hãy ghi rõ câu hỏi hơn giúp bé với ạ ❤️";

    public AIService(GroqClient groqClient,
                     ObjectMapper objectMapper,
                     InternRepository internRepository,
                     SummaryResultRepository summaryResultRepository) {
        this.groqClient = groqClient;
        this.objectMapper = objectMapper;
        this.internRepository = internRepository;
        this.summaryResultRepository = summaryResultRepository;
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
}
    