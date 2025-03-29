package com.calorie_tracker_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.calorie_tracker_backend")
public class CalorieTrackerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CalorieTrackerBackendApplication.class, args);
	}

}
