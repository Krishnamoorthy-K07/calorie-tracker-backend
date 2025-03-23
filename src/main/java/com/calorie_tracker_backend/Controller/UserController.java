package com.calorie_tracker_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.calorie_tracker_backend.DTO.UserDTO;
import com.calorie_tracker_backend.Entity.User;
import com.calorie_tracker_backend.Service.UserService;

@RestController
@RequestMapping("/tracker/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody User user) throws Exception {

        UserDTO result = new UserDTO();
        try {

            result = userService.addUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            throw new Exception("Failed during saving the user due to :" + e.getMessage());
        }

    }

    @GetMapping("/getUserByEmail/{email}")
    public ResponseEntity<UserDTO> findUserByEmail(@PathVariable String email) throws Exception {
        UserDTO userDTO = userService.getUserByEmail(email);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) throws Exception {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

}
