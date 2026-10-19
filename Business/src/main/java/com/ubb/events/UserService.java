package com.ubb.events;

import com.ubb.events.domain.User;
import com.ubb.events.dto.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User save(User user);
    UserResponse register(String firstName, String lastName, String email, String rawPassword);
    Optional<User> findById(Long id);
    List<User> findAll();
}
