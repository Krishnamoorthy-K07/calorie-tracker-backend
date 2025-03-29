package com.calorie_tracker_backend.Service;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CalorieService {

    private static final Logger logger = LoggerFactory.getLogger(CalorieService.class);

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public CalorieService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("null")
    public List<Map<String, Object>> analyzeImageAndGetNutrition(String imageUrl) throws Exception {
        try {
            // **Single Prompt:** AI detects food and provides nutritional data
            String prompt = "Analyze the given image URL and detect all food items. "
                    + "For each detected food, return its nutrient values in JSON format. "
                    + "Each food item should have: \"name\", \"calories\", \"carbs\", \"protein\", \"fat\", and \"fiber\". "
                    + "Return ONLY a JSON object with a `foods` key containing an array. No extra text or explanation.\n"
                    + "Ensure the output is structured as follows:\n"
                    + "{ \"foods\": [ {\"name\": \"Apple\", \"calories\": 52, \"protein\": 0.3, \"fat\": 0.2, \"carbs\": 14, \"fiber\": 2.4} ] }";

            // **Set up headers**
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + aiApiUrl);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // **Construct request body**
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "meta-llama/llama-3.2-11b-vision-instruct:free");

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("role", "user");

            List<Map<String, Object>> contentList = new ArrayList<>();
            contentList.add(Map.of("type", "text", "text", prompt));
            contentList.add(Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)));

            messageContent.put("content", contentList);
            messages.add(messageContent);
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(aiApiUrl, HttpMethod.POST, request, String.class);

            logger.info("AI response: {}", response.getBody());

            // **Handle empty or invalid response**
            if (response.getBody() == null || response.getBody().isBlank()) {
                logger.error("Empty response from AI API.");
                return new ArrayList<>();
            }

            return parseAIResponse2(response.getBody());

        } catch (Exception e) {
            logger.error("Error analyzing food image: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze image", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAIResponse2(String jsonResponse) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            logger.error("Empty AI response.");
            return new ArrayList<>();
        }

        // Log full AI response (for debugging)
        logger.info("Raw AI response: {}", jsonResponse);

        try {
            // Extract the content field from OpenRouter's response
            JsonNode rootNode = mapper.readTree(jsonResponse);

            if (!rootNode.has("choices") || !rootNode.get("choices").isArray() || rootNode.get("choices").isEmpty()) {
                logger.error("Invalid AI response format: {}", jsonResponse);
                return new ArrayList<>();
            }

            // Extract message content (text response)
            JsonNode messageNode = rootNode.get("choices").get(0).get("message");
            if (messageNode == null || !messageNode.has("content")) {
                logger.error("Missing 'content' field in AI response.");
                return new ArrayList<>();
            }

            String content = messageNode.get("content").asText(); // Extract the response text

            // Log content field for debugging
            logger.info("Extracted AI content: {}", content);

            // Find the first JSON block in the content
            Matcher matcher = Pattern.compile("\\{.*\\}", Pattern.DOTALL).matcher(content);
            if (matcher.find()) {
                String jsonText = matcher.group(); // Extract only the JSON part

                // Log extracted JSON
                logger.info("Extracted JSON: {}", jsonText);

                // Convert JSON to List<Map<String, Object>>
                JsonNode contentNode = mapper.readTree(jsonText);
                if (!contentNode.has("foods") || !contentNode.get("foods").isArray()) {
                    logger.error("Invalid 'foods' format in extracted JSON: {}", jsonText);
                    return new ArrayList<>();
                }
                String sanitizedJson = sanitizeJsonResponse(jsonText);
                logger.info("Sanitized JSON: {}", sanitizedJson);

                // Parse JSON into a map

                Map<String, Object> responseMap = mapper.readValue(sanitizedJson,
                        new TypeReference<Map<String, Object>>() {
                        });

                // Extract "foods" list safely
                if (responseMap.containsKey("foods") && responseMap.get("foods") instanceof List) {
                    return (List<Map<String, Object>>) responseMap.get("foods");
                } else {
                    logger.error("Invalid JSON format: 'foods' key missing or not an array");
                    return new ArrayList<>();
                }
            } else {
                logger.error("No valid JSON found in AI response.");
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String sanitizeJsonResponse(String responseBody) {
        // Convert values like 20g to "20g"
        return responseBody.replaceAll("(?<=\"(calories|fat|protein|carbs|fiber)\":\\s*)(\\d+g)", "\"$2\"");
    }

}
