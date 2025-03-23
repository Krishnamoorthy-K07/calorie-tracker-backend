package com.calorie_tracker_backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calorie_tracker_backend.DTO.UserDTO;
import com.calorie_tracker_backend.Entity.User;
import com.calorie_tracker_backend.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDTO addUser(User user) throws Exception {
        try {

            User savedUser = userRepository.save(user);

            UserDTO newUser = new UserDTO();
            newUser.setUserId(savedUser.getUserId());
            newUser.setName(savedUser.getName());
            newUser.setEmail(savedUser.getEmail());
            return newUser;
        } catch (Exception e) {
            throw new Exception("Failed during saving the user due to :" + e.getMessage());
        }
    }

    public UserDTO getUserByEmail(String email) throws Exception {
        try {
            User userFound = userRepository.findUserByEmail(email);
            UserDTO user = new UserDTO();
            user.setUserId(userFound.getUserId());
            user.setName(userFound.getName());
            user.setEmail(userFound.getEmail());

            return user;
        } catch (Exception e) {
            throw new Exception("Failed during finding the user due to :" + e.getMessage());
        }
    }

    public UserDTO getUserById(Long id) throws Exception {
        try {
            User userFound = userRepository.findById(id).orElseThrow(() -> new Exception("NOT FOUND"));
            UserDTO user = new UserDTO();
            user.setUserId(userFound.getUserId());
            user.setName(userFound.getName());
            user.setEmail(userFound.getEmail());

            return user;
        } catch (Exception e) {
            throw new Exception("Failed during finding the user due to :" + e.getMessage());
        }
    }
}
