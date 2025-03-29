package com.calorie_tracker_backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.calorie_tracker_backend.DTO.AuthRequest;
import com.calorie_tracker_backend.DTO.UserDTO;
import com.calorie_tracker_backend.Entity.User;
import com.calorie_tracker_backend.Repository.UserRepository;
import com.calorie_tracker_backend.Util.JwtUtil;

@Service
public class AuthService implements UserDetailsService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash the password
        return userRepository.save(user);
    }

    public String login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getEmail()); // Return JWT Token
    }

    

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // Password should be encoded
                .roles("USER") // Assign default role
                .build();
    }

    public UserDTO getUserProfile(String email) {
    User user = userRepository.findUserByEmail(email);
    if (user == null) {
        throw new UsernameNotFoundException("User not found");
    }

    UserDTO userDTO = new UserDTO();
    userDTO.setUserId(user.getUserId());
    userDTO.setName(user.getName());
    userDTO.setEmail(user.getEmail());

    return userDTO;
}
}

