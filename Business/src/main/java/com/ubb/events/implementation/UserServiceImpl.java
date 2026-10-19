package com.ubb.events.implementation;

import com.ubb.events.UserService;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.domain.User;
import com.ubb.events.dto.UserResponse;
import com.ubb.events.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(String firstName, String lastName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already used");
        }
        User user = new User(null, firstName, lastName, email, passwordEncoder.encode(rawPassword));
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public User save(User user) { return userRepository.save(user); }

    @Override
    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    @Override
    public List<User> findAll() { return userRepository.findAll(); }
}
