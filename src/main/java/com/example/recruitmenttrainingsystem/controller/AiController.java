// src/main/java/com/example/recruitmenttrainingsystem/controller/AiController.java
package com.example.recruitmenttrainingsystem.controller;

import com.example.recruitmenttrainingsystem.dto.AiChatRequest;
import com.example.recruitmenttrainingsystem.service.GroqAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GroqAIService groqAIService;

    public AiController(GroqAIService groqAIService) {
        this.groqAIService = groqAIService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody AiChatRequest request) {
        // Gọi trực tiếp hàm chat(...) trong service
        String reply = groqAIService.chat(request.getMessage());
        return ResponseEntity.ok(reply);
    }
}
