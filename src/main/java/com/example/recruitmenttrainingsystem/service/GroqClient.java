package com.example.recruitmenttrainingsystem.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroqClient {

    @Value("${groq.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    public String sendChatRequest(String prompt) throws Exception {

        String bodyJson = """
        {
          "model": "llama-3.1-8b-instant",
          "messages": [
            { "role": "user", "content": "%s" }
          ]
        }
        """.formatted(prompt.replace("\"", "'"));

        RequestBody requestBody = RequestBody.create(
                bodyJson,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Groq request failed: " + response.code());
            }
            return response.body().string();
        }
    }
}
