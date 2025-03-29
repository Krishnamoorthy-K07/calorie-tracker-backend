package com.calorie_tracker_backend.Controller;

import com.calorie_tracker_backend.DTO.UserDTO;
import com.calorie_tracker_backend.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private AuthService authService;

    @GetMapping
    public UserDTO getProfile(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return authService.getUserProfile(email);
    }
}
