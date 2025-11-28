package com.example.recruitmenttrainingsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    public AIService(GroqClient groqClient, ObjectMapper objectMapper) {
        this.groqClient = groqClient;
        this.objectMapper = objectMapper;
    }

    public String testAI() {
        try {
            String json = groqClient.sendChatRequest(
                    "Viết một đoạn giới thiệu ngắn về hệ thống tuyển dụng thực tập sinh bằng tiếng Việt."
            );

            JsonNode root = objectMapper.readTree(json);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }

            return "Không đọc được phản hồi từ AI.";

        } catch (Exception e) {
            return "Lỗi gọi Groq API: " + e.getMessage();
        }
    }
}
