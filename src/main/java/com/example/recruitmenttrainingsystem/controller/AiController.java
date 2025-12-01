    // src/main/java/com/example/recruitmenttrainingsystem/controller/AiController.java
    package com.example.recruitmenttrainingsystem.controller;

    import com.example.recruitmenttrainingsystem.dto.AiChatRequest;
    import com.example.recruitmenttrainingsystem.service.AIService;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("/api/ai")
    public class AiController {

        private final AIService aiService;

        public AiController(AIService aiService) {
            this.aiService = aiService;
        }

        @PostMapping("/chat")
        public ResponseEntity<String> chat(@RequestBody AiChatRequest request) {
            // Gọi hàm chat(...) trong AIService để luôn có fallback
            String reply = aiService.chat(request.getMessage());
            return ResponseEntity.ok(reply);
        }
    }
