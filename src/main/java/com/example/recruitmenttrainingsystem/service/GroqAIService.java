// src/main/java/com/example/recruitmenttrainingsystem/service/GroqAIService.java
package com.example.recruitmenttrainingsystem.service;

import com.example.recruitmenttrainingsystem.repository.InternRepository;
import com.example.recruitmenttrainingsystem.repository.SummaryResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
public class GroqAIService {

    private final InternRepository internRepository;
    private final SummaryResultRepository summaryResultRepository;   // ⭐ NEW

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key}")
    private String groqApiKey;

    private static final String GROQ_CHAT_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    public GroqAIService(InternRepository internRepository,
                         SummaryResultRepository summaryResultRepository) {   // ⭐ NEW
        this.internRepository = internRepository;
        this.summaryResultRepository = summaryResultRepository;                // ⭐ NEW
    }

    public String chat(String message) {
        if (!StringUtils.hasText(message)) {
            return "Bạn hãy nhập câu hỏi của mình nhé.";
        }

        String normalized = message.trim();
        String msgLower = normalized.toLowerCase();

        // 1) Tổng số thực tập sinh
        if (isAskTotalInterns(msgLower)) {
            return handleInternCount();
        }

        // 2) Hỏi PASS & FAIL cùng lúc
        if (isAskPassAndFail(msgLower)) {
            return handleInternResultStats();
        }

        // 3) Hỏi riêng PASS
        if (isAskPassOnly(msgLower)) {
            long pass = summaryResultRepository       // ⭐ SỬA Ở ĐÂY
                    .countByInternshipResultIgnoreCase("PASS");
            return "Số lượng thực tập sinh có kết quả thực tập PASS hiện tại là "
                    + pass + " bạn.";
        }

        // 4) Hỏi riêng FAIL
        if (isAskFailOnly(msgLower)) {
            long fail = summaryResultRepository       // ⭐ SỬA Ở ĐÂY
                    .countByInternshipResultIgnoreCase("FAIL");
            return "Số lượng thực tập sinh có kết quả thực tập FAIL hiện tại là "
                    + fail + " bạn.";
        }

        // 5) Câu hỏi khác -> gọi Groq
        String systemPrompt = """
                Bạn là trợ lý AI cho hệ thống Quản lý tuyển dụng & đào tạo thực tập sinh.
                Hãy trả lời ngắn gọn, rõ ràng, bằng tiếng Việt, xưng "mình" - "bạn".
                Nếu người dùng hỏi về số lượng thực tập sinh, PASS/FAIL... mà bạn
                không được cung cấp con số từ hệ thống, hãy nói rằng:
                "Mình không có quyền truy cập dữ liệu thống kê chi tiết, bạn có thể xem trực tiếp trên màn hình."
                """;

        return callGroq(systemPrompt, normalized);
    }

    // ========= helper detect câu hỏi (giữ nguyên) =========
    private boolean isAskTotalInterns(String msgLower) { /* y như cũ */
        return (msgLower.contains("tổng số thực tập sinh")
                || msgLower.contains("tổng số tts")
                || msgLower.contains("số lượng thực tập sinh")
                || msgLower.contains("số lượng tts")
                || msgLower.contains("bao nhiêu thực tập sinh")
                || msgLower.contains("bao nhiêu tts"))
                && !msgLower.contains("pass")
                && !msgLower.contains("fail");
    }

    private boolean isAskPassAndFail(String msgLower) { /* y như cũ */
        boolean hasPass = msgLower.contains("pass");
        boolean hasFail = msgLower.contains("fail");
        boolean mentionResult = msgLower.contains("kết quả")
                || msgLower.contains("kết quả thực tập")
                || msgLower.contains("kq thực tập")
                || msgLower.contains("kqua thuc tap");
        return hasPass && hasFail && mentionResult;
    }

    private boolean isAskPassOnly(String msgLower) { /* y như cũ */
        boolean hasPass = msgLower.contains("pass");
        boolean hasFail = msgLower.contains("fail");
        boolean mentionResult = msgLower.contains("kết quả")
                || msgLower.contains("kết quả thực tập")
                || msgLower.contains("kq thực tập");
        return hasPass && !hasFail && mentionResult;
    }

    private boolean isAskFailOnly(String msgLower) { /* y như cũ */
        boolean hasPass = msgLower.contains("pass");
        boolean hasFail = msgLower.contains("fail");
        boolean mentionResult = msgLower.contains("kết quả")
                || msgLower.contains("kết quả thực tập")
                || msgLower.contains("kq thực tập");
        return hasFail && !hasPass && mentionResult;
    }

    // ========= handler dùng DB =========

    private String handleInternCount() {
        long total = internRepository.count();
        return "Số lượng thực tập sinh hiện tại là " + total + ".";
    }

    private String handleInternResultStats() {
        long total = internRepository.count();
        long pass = summaryResultRepository
                .countByInternshipResultIgnoreCase("PASS"); // ⭐ SỬA
        long fail = summaryResultRepository
                .countByInternshipResultIgnoreCase("FAIL"); // ⭐ SỬA
        long other = total - pass - fail;

        StringBuilder sb = new StringBuilder();
        sb.append("Hiện tại hệ thống có tổng cộng ")
                .append(total).append(" thực tập sinh.\n");
        sb.append("- Kết quả thực tập: PASS = ")
                .append(pass).append(" bạn.\n");
        sb.append("- Kết quả thực tập: FAIL = ")
                .append(fail).append(" bạn.");
        if (other > 0) {
            sb.append("\n- Còn lại ").append(other)
                    .append(" thực tập sinh chưa có hoặc có kết quả khác PASS/FAIL.");
        }
        return sb.toString();
    }

    // ========= call Groq API (giữ nguyên) =========
    private String callGroq(String systemPrompt, String userMessage) {
        try {
            String json = objectMapper.createObjectNode()
                    .put("model", "llama-3.1-8b-instant")
                    .putArray("messages")
                    .add(objectMapper.createObjectNode()
                            .put("role", "system")
                            .put("content", systemPrompt))
                    .add(objectMapper.createObjectNode()
                            .put("role", "user")
                            .put("content", userMessage))
                    .toString();

            RequestBody body = RequestBody.create(
                    json, MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(GROQ_CHAT_URL)
                    .addHeader("Authorization", "Bearer " + groqApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "Gọi Groq API thất bại, mã lỗi: " + response.code();
                }

                String responseBody = response.body() != null
                        ? response.body().string()
                        : "";

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode msg = choices.get(0).path("message");
                    String content = msg.path("content").asText();
                    if (StringUtils.hasText(content)) {
                        return content.trim();
                    }
                }
                return "Mình chưa nhận được nội dung trả lời từ Groq.";
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Có lỗi kết nối đến Groq API: " + e.getMessage();
        }
    }
}
