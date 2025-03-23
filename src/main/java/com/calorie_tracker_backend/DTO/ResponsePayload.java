package com.calorie_tracker_backend.DTO;

import java.util.List;
import java.util.Map;



import lombok.Data;


@Data
public class ResponsePayload  {

    private String responseMessage;
    private String responseCode;
    private List<Map<String, Object>> nutritionData;
}
