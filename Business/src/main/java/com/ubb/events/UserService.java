package com.ubb.events;

import com.ubb.events.domain.User;
import com.ubb.events.dto.UserResponse;
import com.ubb.events.dto.LoginResponse;
import com.ubb.events.dto.UserProfileResponse;
import com.ubb.events.dto.UpdateUserProfileRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);
    UserResponse register(String firstName, String lastName, String email, String rawPassword);
    Optional<User> findById(Long id);
    List<User> findAll();
    LoginResponse login(String email, String rawPassword);
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request);
}
