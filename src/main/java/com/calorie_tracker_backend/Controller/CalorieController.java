package com.calorie_tracker_backend.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.calorie_tracker_backend.DTO.ResponsePayload;
import com.calorie_tracker_backend.Service.CalorieService;

@RestController
@RequestMapping("/tracker")
public class CalorieController {

    private static final Logger logger = LoggerFactory.getLogger(CalorieController.class);
    
    private final CalorieService calorieService;

    public CalorieController(CalorieService calorieService) {
        this.calorieService = calorieService;
    }

    // 🔹 API: Analyze food from an image URL
    @PostMapping("/analyze-food")
    public ResponseEntity<ResponsePayload> analyzeImageUrl(@RequestParam String imageUrl) {
        List<Map<String, Object>> nutritionData = new ArrayList<>();
        ResponsePayload response = new ResponsePayload();
        try {
            // Step 1: Identify multiple food items from the image
            // List<String> foodNames = calorieService.identifyFoodFromImage(imageUrl);

            // // Handle case where no food is detected
            // if (foodNames.isEmpty()) {
            //     logger.warn("No food detected in the image.");
            //     response.setNutritionData(nutritionData);
            //     response.setResponseCode("ERROR");
            //     response.setResponseMessage("No food detected in the image.");
            //     return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            // }

            // logger.info("Detected food items: {}", foodNames);

            // // Step 2: Fetch nutrition data for each food item and filter required values
            // nutritionData = calorieService.getFoodCalories(foodNames);

            nutritionData = calorieService.analyzeImageAndGetNutrition(imageUrl);
            response.setNutritionData(nutritionData);
            response.setResponseCode("SUCCESS");
            response.setResponseMessage("Successfully fetched nutrientional value of the foods");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            logger.error("Error analyzing food image: {}", e.getMessage(), e);

            response.setNutritionData(nutritionData);
            response.setResponseCode("ERROR");
            response.setResponseMessage("Error analyzing food image : " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
