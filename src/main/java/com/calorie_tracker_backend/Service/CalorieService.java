package com.calorie_tracker_backend.Service;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CalorieService {

    private static final String AI_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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"; // Replace
                                                                                                                       // with
                                                                                                                       // your
                                                                                                                       // key

    private static final String NUTRITION_API_URL = "https://api.nal.usda.gov/fdc/v1/foods/search?query=";
    private static final String NUTRITION_API_KEY = "EqstCc6dGEibpjWAnTpBszj5ymHZZFWWdywWIR24"; // Replace with USDA API
                                                                                                // key

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔹 Step 1: Send Image to AI for Food Identification
    public List<String> identifyFoodFromImage(String imageUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "meta-llama/llama-3.2-11b-vision-instruct:free");

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("role", "user");

        List<Map<String, Object>> contentList = new ArrayList<>();
        contentList.add(Map.of("type", "text", "text",
                "Analyze this image and list all recognizable food items in a structured List format. ONLY return exact food names, do not describe the scene. Example output: {\"foods\": [\"Grilled Chicken\", \"Rice\", \"Salad\"]}."));
        contentList.add(Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)));

        messageContent.put("content", contentList);
        messages.add(messageContent);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(AI_API_URL, HttpMethod.POST, request, String.class);

        return parseAIResponse(response.getBody());
    }

    // 🔹 Step 2: Extract AI Response (Food Name)
    private List<String> parseAIResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);

            // Extract JSON object that AI returns
            String aiResponse = root.path("choices").get(0).path("message").path("content").asText();

            // Convert response to JSON and extract food names
            JsonNode foodJson = objectMapper.readTree(aiResponse);
            List<String> foodItems = new ArrayList<>();
            for (JsonNode food : foodJson.path("foods")) {
                foodItems.add(food.asText());
            }

            return foodItems;
        } catch (Exception e) {
            return List.of("Error identifying food");
        }
    }

    // 🔹 Step 3: Query Nutrition API for Calories
    public List<Map<String, Object>> getFoodCalories(List<String> foodNames) {
        List<Map<String, Object>> foodNutritionList = new ArrayList<>();

        for (String foodName : foodNames) {
            String url = NUTRITION_API_URL + foodName.trim() + "&api_key=" + NUTRITION_API_KEY;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Map<String, Object> nutritionData = parseNutritionResponse(response.getBody());

            foodNutritionList.add(nutritionData);
        }
        return foodNutritionList;
    }

    // 🔹 Step 4: Extract Calories & Nutrition Details
    private Map<String, Object> parseNutritionResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
    
            // Check if "foods" array is present and has at least one entry
            if (!root.has("foods") || root.get("foods").isEmpty()) {
                return Map.of("error", "No food data found");
            }
    
            JsonNode food = root.path("foods").get(0);
            Map<String, Object> nutrition = new HashMap<>();
    
            // Extract food name
            String foodName = food.path("description").asText();
            nutrition.put("name", foodName.isEmpty() ? "Unknown Food" : foodName);
    
            // Extract nutrients dynamically
            JsonNode nutrients = food.path("foodNutrients");
            if (nutrients.isArray()) {
                for (JsonNode nutrient : nutrients) {
                    String nutrientName = nutrient.path("nutrientName").asText();
                    double value = nutrient.path("value").asDouble(0.0); // Default to 0.0 if missing
    
                    switch (nutrientName) {
                        case "Energy":
                            nutrition.put("calories", value);
                            break;
                        case "Protein":
                            nutrition.put("protein", value);
                            break;
                        case "Total lipid (fat)":
                            nutrition.put("fat", value);
                            break;
                        case "Carbohydrate, by difference":
                            nutrition.put("carbs", value);
                            break;
                    }
                }
            }
    
            // Ensure all fields are present, even if missing in API
            nutrition.putIfAbsent("calories", 0.0);
            nutrition.putIfAbsent("protein", 0.0);
            nutrition.putIfAbsent("fat", 0.0);
            nutrition.putIfAbsent("carbs", 0.0);
    
            return nutrition;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Error parsing nutrition data");
        }
    }
    
    
    
    
}
