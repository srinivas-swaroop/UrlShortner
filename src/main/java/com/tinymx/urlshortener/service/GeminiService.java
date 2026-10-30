package com.tinymx.urlshortener.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class GeminiService {
    private final String apiKey;
    private final String model;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiService(@Value("${gemini.api-key:}") String apiKey,
                         @Value("${gemini.model:gemini-2.5-flash}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public String createLinkInsight(String url, String context) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("Gemini is not configured. Add GEMINI_API_KEY to your environment.");
        String prompt = "Give a short, clear title and one-sentence description for this link. Do not claim you visited the page. " +
                "URL: " + url + "\nUser context: " + (context == null ? "" : context) +
                "\nReturn plain text with lines starting Title: and Description:.";
        Map<String, Object> body = Map.of("contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", prompt)})});
        JsonNode response = restClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) throw new IllegalStateException("Gemini returned an empty response.");
        JsonNode text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text.isMissingNode() || text.asText().isBlank()) throw new IllegalStateException("Gemini could not generate an insight. Try again.");
        return text.asText();
    }
}
