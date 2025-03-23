package com.calorie_tracker_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.calorie_tracker_backend.Entity.User;
@Repository
public interface UserRepository extends JpaRepository<User,Long>{

    User findUserByEmail(String email);
    
}
