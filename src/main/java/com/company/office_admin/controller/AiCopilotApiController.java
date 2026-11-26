package com.company.office_admin.controller;

import com.company.office_admin.dto.AiGenerationResponse;
import com.company.office_admin.dto.AiPromptRequest;
import com.company.office_admin.service.AiCopilotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiCopilotApiController {

    private final AiCopilotService aiCopilotService;

    public AiCopilotApiController(AiCopilotService aiCopilotService) {
        this.aiCopilotService = aiCopilotService;
    }

    @PostMapping("/generate")
    public ResponseEntity<AiGenerationResponse> generateContent(@RequestBody AiPromptRequest request) {
        AiGenerationResponse response = aiCopilotService.generateCaptions(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/hashtags")
    public ResponseEntity<List<String>> suggestHashtags(@RequestBody Map<String, String> payload) {
        String content = payload.getOrDefault("content", payload.getOrDefault("topic", ""));
        String platform = payload.getOrDefault("platform", "general");
        List<String> tags = aiCopilotService.suggestHashtags(content, platform);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/hooks")
    public ResponseEntity<List<String>> generateHooks(@RequestBody Map<String, String> payload) {
        String topic = payload.getOrDefault("topic", "");
        String tone = payload.getOrDefault("tone", "viral");
        List<String> hooks = aiCopilotService.generateViralHooks(topic, tone);
        return ResponseEntity.ok(hooks);
    }
}
