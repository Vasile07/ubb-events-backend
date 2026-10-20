package com.ubb.events.implementation;

import com.ubb.events.UserService;
import com.ubb.events.common.errors.ConflictException;
import com.ubb.events.common.errors.InvalidCredentialsException;
import com.ubb.events.common.errors.NotFoundException;
import com.ubb.events.common.security.JwtTokenService;
import com.ubb.events.domain.User;
import com.ubb.events.dto.UserResponse;
import com.ubb.events.dto.LoginResponse;
import com.ubb.events.dto.UserProfileResponse;
import com.ubb.events.dto.MembershipResponse;
import com.ubb.events.persistence.MembershipRepository;
import com.ubb.events.domain.MembershipStatus;
import com.ubb.events.domain.MembershipRole;
import com.ubb.events.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final MembershipRepository membershipRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, MembershipRepository membershipRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<MembershipResponse> profileMemberships = membershipRepository.findAllByUserId(userId).stream()
                .filter(membership -> membership.getRole() != null)
                .filter(membership -> !(membership.getStatus() == MembershipStatus.PENDING
                        && membership.getRole() == MembershipRole.MEMBER))
                .map(membership -> new MembershipResponse(
                        membership.getOrganization().getId(),
                        membership.getStatus() == MembershipStatus.PENDING
                                ? MembershipRole.MEMBER
                                : membership.getRole()))
                .toList();
        return new UserProfileResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), profileMemberships);
    }

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

    @Override
    public LoginResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .filter(candidate -> passwordEncoder.matches(rawPassword, candidate.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return new LoginResponse(jwtTokenService.issue(user.getId()));
    }
}
