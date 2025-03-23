package com.calorie_tracker_backend.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class AIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";

    public String analyzeImage(String imageUrl) {
        RestTemplate restTemplate = new RestTemplate();

        // Request headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("HTTP-Referer", "YOUR_SITE_URL"); // Optional
        headers.set("X-Title", "YOUR_SITE_NAME"); // Optional

        // Construct request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "meta-llama/llama-3.2-11b-vision-instruct:free");

        // Construct messages list
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("role", "user");

        List<Map<String, Object>> contentList = new ArrayList<>();
        contentList.add(Map.of("type", "text", "text", "What is in this image?"));
        contentList.add(Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)));

        messageContent.put("content", contentList);
        messages.add(messageContent);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Make the request
        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, String.class);

        // Parse and return AI response
        return parseAIResponse(response.getBody());
    }

    private String parseAIResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "Error parsing AI response";
        }
    }
}
