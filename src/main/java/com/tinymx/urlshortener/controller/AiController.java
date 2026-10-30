package com.tinymx.urlshortener.controller;

import com.tinymx.urlshortener.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final GeminiService geminiService;
    public AiController(GeminiService geminiService) { this.geminiService = geminiService; }

    @PostMapping("/link-insight")
    public ResponseEntity<Map<String, String>> linkInsight(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        System.out.println("Prining URL:"+url);
        if (url == null || url.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Please provide a URL."));
        try {
            String insight = geminiService.createLinkInsight(url, request.getOrDefault("context", ""));
            System.out.println("Insights:"+insight);
            return ResponseEntity.ok(Map.of("insight", insight));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(503).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(502).body(Map.of("error", "Gemini request failed. Check the model name, API key and network connection."));
        }
    }
}
