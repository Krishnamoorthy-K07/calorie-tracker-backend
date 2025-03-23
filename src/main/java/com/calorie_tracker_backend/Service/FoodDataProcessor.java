package com.calorie_tracker_backend.Service;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class FoodDataProcessor {

    public  Map<String, Object> processFoodApiResponse(String jsonResponse) {
        Map<String, Object> cleanedData = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray foodsArray = jsonObject.getJSONArray("foods");

            if (foodsArray.length() > 0) {
                JSONObject food = foodsArray.getJSONObject(0);

                // Extract relevant details
                String foodName = food.optString("description", "Unknown Food");
                double calories = extractNutrientValue(food, 1008); // Energy (Kcal)
                double protein = extractNutrientValue(food, 1003);
                double fat = extractNutrientValue(food, 1004);
                double carbs = extractNutrientValue(food, 1005);
                double fiber = extractNutrientValue(food, 1079);
                double sugar = extractNutrientValue(food, 2000);
                double calcium = extractNutrientValue(food, 1087);
                double iron = extractNutrientValue(food, 1089);
                double sodium = extractNutrientValue(food, 1093);
                double cholesterol = extractNutrientValue(food, 1253);
                double vitaminC = extractNutrientValue(food, 1162);
                double vitaminA = extractNutrientValue(food, 1104);
                double servingSize = food.optDouble("servingSize", 100); // Default 100g if not found

                // Construct cleaned JSON response
                cleanedData.put("food", Map.of(
                        "name", foodName,
                        "calories", calories,
                        "macros", Map.of(
                                "protein", protein,
                                "fat", fat,
                                "carbohydrates", carbs),
                        "nutrients", Map.of(
                                "fiber", fiber,
                                "sugar", sugar,
                                "calcium", calcium,
                                "iron", iron,
                                "sodium", sodium,
                                "cholesterol", cholesterol,
                                "vitaminC", vitaminC,
                                "vitaminA", vitaminA),
                        "serving_size", Map.of(
                                "amount", servingSize,
                                "unit", "g")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            cleanedData.put("error", "Failed to process food data.");
        }

        return cleanedData;
    }

    private static double extractNutrientValue(JSONObject food, int nutrientId) {
        JSONArray nutrients = food.getJSONArray("foodNutrients");
        for (int i = 0; i < nutrients.length(); i++) {
            JSONObject nutrient = nutrients.getJSONObject(i);
            if (nutrient.getInt("nutrientId") == nutrientId) {
                return nutrient.optDouble("value", 0.0);
            }
        }
        return 0.0;
    }
}
